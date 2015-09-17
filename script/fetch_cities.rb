#!/usr/bin/env ruby

##########################
## fetch_cities.rb
##
## This ruby script is used to fetch en exhaustive list of cities around the world
## using the geonames.org services, and add it to an Algolia index.
## 
## Usage: ./fetch_cities.rb
##
##########################


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
TMP_DIR 					= './tmp'
GEONAMES_DOMAIN 			= 'download.geonames.org'
GEONAMES_DUMP_PATH 			= '/export/dump/'
GEONAMES_COUNTRY_INFO_PATH 	= '/countryInfoCSV'

# Config: geonames.org country features database fields
GNM_ID				= 0
GNM_NAME			= 1
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
## HELPER METHODS
##########################

###
# is_http_error: indicates if a server response code is an HTTP error
# @return: true on error, or false
###
def is_http_error response
	return (response.code.to_i) / 100 != 2
end

###
# get_country_name: retrieves a country's full name using geonames.org API based on its 2 letters code
#
# http: The Net:HTTP instance connected to the geonames.org services
# country_code: 2 letters country code
#
# @return: The retrieved country name or the country code if unable to retrieve it
###
def get_country_name http, country_code
	# Fetch country information from geonames server based on country code
	country_info_path = "#{GEONAMES_COUNTRY_INFO_PATH}?user=demo&country=#{country_code}"
	country_info = http.get(country_info_path)
	if is_http_error country_info 
		puts "\tget_country_name: Received error #{country_info.code} from server"
		return country_code
	end

	entry = country_info.body.split("\n")[1..-1] #Remove header line from response
	if entry && entry.length == 1
		entry = entry[0].split("\t")
		if (entry.length > 4 && entry[4].length > 0) # 4 is the index of the country name in retrieved table
			return entry[4];
		end	
	end

	return country_code
end

###
# safe_utf8_string: Forces UTF-8 encoding on input string.
# This prevents the raised Encoding::UndefinedConversionError when data is encoded to json by the Algolia API client
#
# string: string to encode
# @return: safely encoded string
###
def safe_utf8_string string
	s = string.force_encoding("utf-8")
	return s.encode("utf-8", :undef => :replace)
end

###
# zip_to_records: extracts a zip file data and parses its txt content from the geonames.org database format to
# Algolia's index format
#
# zip_path: path to the zip file to extract
# country_name: The name of the country the parsed entries should be associated with
# limit: Buffer limit when parsing entries
###
def zip_to_records zip_path, country_name, limit = 1000
	searched_txt_file = "#{File.basename(zip_path, ".*")}.txt"
	records = []

	# Read zip file content
	Zip::File.open(zip_path) do |zip_file|
		found = false
		zip_file.each do |f|

			# for each file in zip, check for a txt file named <country_code>.zip
			if f.name == searched_txt_file and f.file? 
				found = true

				# Read each line of the txt file
				f.get_input_stream.each_line do |line|

					line.strip! # Trim the line from any extra characters (\n, ...)
					data = line.split("\t") # txt file is CSV format, with \t as separator between fields

					# Ensure data validity
					if data.length >= GNM_TIMESTAMP + 1 && 
						data[GNM_FEATURE_CLASS] == 'P' && # P means feature is a city or village
						['PPL', 'PPLC', 'PPLA', 'PPLA2', 'PPLA3', 'PPLA4', 'PPLF', 'PPLL', 'PPLR', 'PPLS'].include?(data[GNM_FEATURE_CODE]) && # PPL* means feature is a populated place
						data[GNM_POPULATION].to_i > 0

						# Convert retrieved data to a hash that is suitable for an Algolia index
						hash = {
							objectID: data[GNM_ID],
							name: safe_utf8_string(data[GNM_NAME]),
							country: safe_utf8_string(country_name),
							population: data[GNM_POPULATION],
							timezone: safe_utf8_string(data[GNM_TIMEZONE]),
							timestamp: data[GNM_TIMESTAMP],
							_geoloc: {
								lat: data[GNM_LAT].to_f,
								lng: data[GNM_LNG].to_f
							},
							_raw: safe_utf8_string(line)
						}

						# Save the parsed record
						records.push hash

						# Every `limit` records reached, call the delegate block
						if (records.length == limit) 
							yield records
							records.clear
						end
					end
				end
			end
		end
		if !found 
			puts "WARNING: Could not find data file #{searched_txt_file} in #{zip_path}"
		end
	end

	# Call the delegate block with remaining records if any
	if records.length
		yield records
	end
end




##########################
## FETCHING CITIES
##########################

# Initialize Algolia API client
Algolia.init :application_id => "CC37YOB5YL", :api_key => "08819e0217baeb114f3026c444009ae9"
index = Algolia::Index.new(ALGOLIA_INDEX_NAME);

# Ensure tmp directory exists and is empty
FileUtils.rm_rf(TMP_DIR)
FileUtils.mkdir_p(TMP_DIR)

# Connect to the geonames.org services
puts "Fetching cities list..."
Net::HTTP.start(GEONAMES_DOMAIN) do |http|

	# Fetch the list of dumped countries features on geonames server
    resp = http.get(GEONAMES_DUMP_PATH)
	if is_http_error resp 
		puts "Received error #{resp.code} from geonames server"
		exit 1
	end

	# Scan response body html for each link with href matching <country_code>.zip
    body = resp.body
    body.scan(/<a href="([A-Z]*.zip)">/).each do |zip_file|

    	# DELETE ME
    	# if zip_file[0] != "FR.zip" 
    	# 	puts "Ignoring #{zip_file}..."
    	# 	next 
    	# end
    	# END DELETE ME

    	# Retrieve country information based on zip file name
    	country_code = File.basename(zip_file[0], ".*")
    	print "Fetching country information for code #{country_code}..."
    	country_name = get_country_name http, country_code
    	puts "Done: #{country_name}"

    	# Download zip file to tmp directory
    	zip_path = "#{GEONAMES_DUMP_PATH}#{zip_file[0]}"
    	download_path = "#{TMP_DIR}/#{zip_file[0]}"
		print "Downloading http://#{GEONAMES_DOMAIN}#{zip_path}..."
    	File.open(download_path, "w") do |f|
    		http.request_get(zip_path) do |resp|
    			if is_http_error resp
    				puts "\t Received error #{resp.code} from server"
    				next
    			end
        		resp.read_body do |segment|
            		f.write(segment)
            	end
        	end
    	end
        puts "Done."

        # Read zip data and add it to Algolia index
        puts "Extracting data from #{download_path}..."
        zip_to_records(download_path, country_name) do |records|
        	print "\tSaving #{records.length} records to index..."
        	index.save_objects records
        	puts "Done"
        end

        # Deletes downloaded zip file from tmp directory
        print "Done, deleting #{download_path}..."
        FileUtils.rm_rf download_path
        puts "Done"
        puts ""
        
    end
end
