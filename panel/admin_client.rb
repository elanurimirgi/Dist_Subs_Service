require 'socket'
require 'json'

class AdminClient
  def initialize
    @host = 'localhost'
    @ports = [5001, 5002, 5003]  # List of servers to check capacity from
  end

  def send_message(message, port)
    begin
      socket = TCPSocket.new(@host, port)
      socket.puts(message)
      response = socket.gets.chomp
      socket.close
      return response
    rescue StandardError => e
      puts "Error connecting to server on port #{port}: #{e.message}"
    end
  end

  def get_capacity_info
    capacity_info = []
    @ports.each do |port|
      message = "CAPACITY_INFO"
      response = send_message(message, port)
      if response&.include?("INVALID_REQUEST")
        puts "Invalid response from server #{port}"
      elsif response
        begin
          # JSON formatında parse et
          server_info = JSON.parse(response, symbolize_names: true)
          capacity_info << server_info if server_info[:server_id] && server_info[:server_status] && server_info[:timestamp]
        rescue JSON::ParserError
          puts "Failed to parse JSON from server #{port}: #{response}"
        end
      end
    end
    capacity_info
  end
end

# Main program to fetch server capacity info in a loop
admin_client = AdminClient.new
loop do
  puts "Fetching server capacities..."
  capacity_info = admin_client.get_capacity_info
  puts "Server Capacities:"
  capacity_info.each do |info|
    puts "Server #{info[:server_id]}: Status: #{info[:server_status]}, Timestamp: #{info[:timestamp]}"
  end
  sleep(5)  # Wait for 5 seconds before the next query
end
