#!/usr/bin/env ruby

require 'rubygems'

require 'fileutils'
require 'net/http'
require 'algoliasearch'
require 'zip'

##########################
## CONFIGURATION
##########################

# Config: Algolia API
ALGOLIA_APP_ID 		= 'CC37YOB5YL'
ALGOLIA_API_KEY 	= '08819e0217baeb114f3026c444009ae9'
ALGOLIA_INDEX_NAME	= 'dev_cities'

# Config: Fetching data from geonames.org
TMP_DIR 		= './tmp'
GEONAMES_DOMAIN = 'download.geonames.org'
GEONAMES_PATH 	= '/export/dump/'
GEONAMES_URL 	= 'http://download.geonames.org/export/dump/'

# Config: geonames.org fields
GNM_ID				= 0
GNM_NAME			= 2
GNM_LAT				= 4
GNM_LNG				= 5
GNM_FEATURE_CLASS	= 6
GNM_FEATURE_CODE	= 7
GNM_COUNTRY_CODE	= 8
GNM_POPULATION		= 14
GNM_ELEVATION		= 15
GNM_TIMEZONE		= 17
GNM_TIMESTAMP		= 18



##########################
## UPDATE CITIES
##########################

Algolia.init :application_id => "CC37YOB5YL", :api_key => "08819e0217baeb114f3026c444009ae9"
index = Algolia::Index.new(ALGOLIA_INDEX_NAME);

FileUtils.rm_rf(TMP_DIR)
FileUtils.mkdir_p(TMP_DIR)

def zip_to_records zip_path, limit = 1000
	searched_txt_file = "#{File.basename(zip_path, ".*")}.txt"
	records = []
	Zip::File.open(zip_path) do |zip_file|
		found = false
		zip_file.each do |f|
			if f.name == searched_txt_file and f.file? then
				found = true
				f.get_input_stream.each_line do |line|
					line.strip!
					data = line.split("\t")

					# if (data[GNM_NAME] == "Mayenne") then
					# 	puts "Found MAYENNE !! : #{data}"
					# end

					if data.length >= GNM_TIMESTAMP + 1 && 
						data[GNM_FEATURE_CLASS] == 'P' && # P means feature is a city or village
						['PPL', 'PPLC', 'PPLA', 'PPLA2', 'PPLA3', 'PPLA4'].include?(data[GNM_FEATURE_CODE]) && # PPL* means feature is a populated place
						data[GNM_POPULATION].to_i > 0 # We are only interested in features that actually contain inhabitants

						hash = {
							objectID: data[GNM_ID],
							name: data[GNM_NAME],
							lat: data[GNM_LAT],
							lng: data[GNM_LNG],
							country: data[GNM_COUNTRY_CODE],
							population: data[GNM_POPULATION],
							timezone: data[GNM_TIMEZONE],
							timestamp: data[GNM_TIMESTAMP]
						}

						records.push hash
						if (records.length == limit) then
							yield records
							records.clear
						end
					end
				end
			end
		end
		if !found then
			puts "WARNING: Could not find data file #{searched_txt_file} in #{zip_path}"
		end
	end

	if records.length
		yield records
	end
end

puts "Fetching cities list..."
Net::HTTP.start(GEONAMES_DOMAIN) do |http|
	#TODO: Handle HTTP errors (404, ...)
    resp = http.get(GEONAMES_PATH)
    body = resp.body
    body.scan(/<a href="([A-Z]*.zip)">/).each do |zip_file|
    	# For each downloaded zip file, read txt content and add it to algolia index
    	zip_path = "#{GEONAMES_PATH}#{zip_file[0]}"
    	download_path = "#{TMP_DIR}/#{zip_file[0]}"

    	# DELETE ME
    	if zip_file[0] != "FR.zip" then
    		puts "Ignoring #{zip_file}..."
    		next 
    	end
    	# END DELETE ME

		print "Downloading http://#{GEONAMES_DOMAIN}#{zip_path}..."
    	File.open(download_path, "w") do |f|
    		http.request_get(zip_path) do |resp|
    			#TODO: Handle HTTP errors (404, ...)
        		resp.read_body do |segment|
            		f.write(segment)
            	end
        	end
    	end
        puts "Done."

        puts "Extracting data from #{download_path}..."
        zip_to_records(download_path) do |records|
        	# puts "Found cities: #{records}"
        	print "\tSaving #{records.length} records to index..."
        	index.save_objects records
        	puts "Done"
        end
        print "Done, deleting #{download_path}..."
        FileUtils.rm_rf download_path
        puts "Done"
        puts ""
        
    end
end
