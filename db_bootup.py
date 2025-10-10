# This script is checking for missing databases in a Docker container running PostgreSQL.
# It accepts the arguments container_id, user_name, and password. 
# It requires sudo permissions to run Docker commands.

# Example usage:
# sudo python3 db_bootup.py <container_id> <user_name> <password> <root_path>
# where <container_id> is the ID of the running PostgreSQL container,
# <user_name> is the PostgreSQL user name (default is "root"),
# <password> is the PostgreSQL password (default is "root"),
# <root_path> is the path to the backend directory (default is current directory).
# finds all databases in the backend directory

import os
import sys
import subprocess


##
## Helper functions
## 

"""
This will extract the database names from the Java properties files and Python config files in the backend directory.
"""
def extract_database_from_java_properties(file_path):
    databases = []
    with open(file_path, 'r') as file:
        for line in file:
            if line.startswith('spring.datasource.url='):
                # Extract the database URL
                url = line.split('=')[1].strip()
                # the last part is the database name, e.g., quiz_service
                db = url.split('/')[-1]
                databases.append(db)
    return databases

"""
This will extract the database names from the Python config files in the backend directory.
"""
def extract_database_from_python_config(file_path):
    # database:
        #  connection_string: "user=root password=root host=database port=5432 dbname=docprocai_service"
    databases = []
    with open(file_path, 'r') as file:
        for line in file:
            if 'connection_string' in line:
                # Extract the connection string
                connection_string = line.split(':')[1].strip().strip('"')
                # Split by spaces and find the dbname
                parts = connection_string.split(' ')
                for part in parts:
                    if part.startswith('dbname='):
                        db = part.split('=')[1]
                        databases.append(db)
    return databases

"""
search for all databases in the backend directory
"""
def find_databases(backend_dir):
    # find all projects and read the src/main/resources/application-dev.properties and extract the database URL
    
    directories = [d for d in os.listdir(backend_dir) if os.path.isdir(os.path.join(backend_dir, d))]
    
    # filter only directories that end with '_service'
    directories = [d for d in directories if d.endswith('_service')]
    
    # filter directories that contain a 'src/main/resources/application-dev.properties' file
    java_directories = [d for d in directories if os.path.exists(os.path.join(backend_dir, d, 'src', 'main', 'resources', 'application-dev.properties'))]

    # contains config.yaml
    python_directories = [d for d in directories if os.path.exists(os.path.join(backend_dir, d, 'config.yaml'))]

    databases = []   
    for d in java_directories:
        file_path = os.path.join(backend_dir, d, 'src', 'main', 'resources', 'application-dev.properties')
        databases.extend(extract_database_from_java_properties(file_path))

    for d in python_directories:
        file_path = os.path.join(backend_dir, d, 'config.yaml')
        databases.extend(extract_database_from_python_config(file_path))

    return databases
"""
This function checks if the specified Docker container is running.
"""
def is_container_running(container_id):    
    try:
        result = subprocess.run(['docker', 'ps', '-q', '-f', f'id={container_id}'], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        # check if successful
        if result.returncode != 0:
            print(f"Error running docker command: {result.stderr.decode().strip()}")
            return False
        return bool(result.stdout.strip())
    except Exception as e:
        print(f"Error checking container status: {e}")
        return False

"""
This function creates a database in the PostgreSQL container.
It uses the provided user name and password to authenticate.
"""
def create_database(db_name):
    try:
        result = subprocess.run(['docker', 'exec', container_id, 'psql', '-U', user_name, '-c', f'CREATE DATABASE "{db_name}";'],
                                stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        if result.returncode != 0:
            print(f"Error creating database {db_name}: {result.stderr.decode().strip()}")
        else:
            print(f"Database {db_name} created successfully.")
    except Exception as e:
        print(f"Error creating database {db_name}: {e}")

"""
This function retrieves the list of existing databases in the PostgreSQL container.
"""
def get_existing_dbs():
   try:
        result = subprocess.run(['docker', 'exec', container_id, 'psql', '-U', user_name, '-tAc', "SELECT datname FROM pg_database WHERE datistemplate = false;"],
                                stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        if result.returncode != 0:
            print(f"Error running docker command: {result.stderr.decode().strip()}")
            return []
        # decode the output and split by new lines
        existing_dbs = result.stdout.decode().strip().split('\n')
        return [db.strip() for db in existing_dbs if db.strip()]
   except Exception as e:
        print(f"Error getting existing databases: {e}")
        return []

#
# main script starts here
# 
container_id = ""
user_name = "root"
password = "root"
root_path = "."

# read arguments from command line
if len(sys.argv) > 1:
    container_id = sys.argv[1]
else:
    print("No container ID provided")
    exit(1)
if len(sys.argv) > 2:
    user_name = sys.argv[2]
if len(sys.argv) > 3:
    password = sys.argv[3]
if len(sys.argv) > 4:
    root_path = sys.argv[4]

databases = find_databases(root_path)

# check if container is running
# run docker ps -a
print("Checking if the database container is running...")
c_is_running = is_container_running(container_id)
if c_is_running:    
    print(f"Container {container_id} is running.")
else:
    print(f"Container {container_id} is not running. Please start the container.")
    exit(1)


print("Fetching existing databases from the container...")
# get all existing databases
existing_databases = get_existing_dbs()

print("Existing databases in the container:")
for db in existing_databases:
    print(f"- {db}")

# check databases that are missing
missing_databases = [db for db in databases if db not in existing_databases]
if missing_databases:
    print("Missing databases:")
    for db in missing_databases:
        print(f"- {db}")
else:
    print("No missing databases.")

for db in missing_databases:
    print(f"Creating missing database: {db}")
    create_database(db)