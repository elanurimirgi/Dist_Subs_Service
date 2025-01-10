import socket
import json
import time
import matplotlib.pyplot as plt
from datetime import datetime

class Plotter:
    def __init__(self):
        self.host = 'localhost'
        self.ports = [5001, 5002, 5003]  # Sunucuların portları
        self.server_data = {1: [], 2: [], 3: []}  # Sunucuların kapasite verileri
        self.timestamps = {1: [], 2: [], 3: []}  # Sunucuların zaman damgaları

    # Sunuculardan kapasite verisini alır
    def collect_data(self):
        for port in self.ports:
            try:
                socket_connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                socket_connection.connect((self.host, port))
                socket_connection.sendall(b"CAPACITY_INFO")

                # Sunucudan gelen yanıtı al
                response = socket_connection.recv(1024).decode('utf-8')
                data = self.parse_capacity_response(response)

                if data:
                    server_id = data['server_id']
                    server_status = data['server_status']
                    timestamp = data['timestamp']

                    # Kapasite ve zaman damgasını kaydet
                    self.server_data[server_id].append(server_status)
                    self.timestamps[server_id].append(self.convert_timestamp_to_time(timestamp))
                else:
                    print(f"Invalid response from server on port {port}")
            except Exception as e:
                print(f"Error connecting to server on port {port}: {e}")
            finally:
                socket_connection.close()

    # JSON formatındaki kapasite yanıtını parse etme
    def parse_capacity_response(self, response):
        try:
            return json.loads(response)
        except json.JSONDecodeError:
            print("Failed to parse the response.")
            return None

    # UNIX epoch zamanını okunabilir formata dönüştürme
    def convert_timestamp_to_time(self, timestamp):
        return datetime.utcfromtimestamp(timestamp).strftime('%Y-%m-%d %H:%M:%S')

    # Verileri görselleştirir
    def plot_data(self):
        # Eğer hiç veri yoksa grafiğini çizmeyi durdur
        if not any(self.server_data.values()):
            print("No data collected yet. Waiting for server responses...")
            return

        plt.figure(figsize=(10, 6))

        # Her sunucu için veriyi plotla
        for server_id, capacities in self.server_data.items():
            if capacities:
                plt.plot(self.timestamps[server_id], capacities, label=f"Server {server_id}")

        plt.xlabel('Timestamp')
        plt.ylabel('Capacity')
        plt.title('Server Capacity Over Time')
        plt.legend()
        plt.xticks(rotation=45)
        plt.tight_layout()
        plt.show()

if __name__ == "__main__":
    plotter = Plotter()
    while True:
        print("Collecting data...")
        plotter.collect_data()
        plotter.plot_data()
        time.sleep(5)
