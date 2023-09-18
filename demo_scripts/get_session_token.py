# This script allows you to get a session token for a user
#
# Usage: python get_session_token.py <user_name> <user_password> (--local | --deployed)
#
# If no arguments are given, the user will be prompted for the user name and password
# An authorization header will be copied to the clipboard
# If the --local flag is given, the script will use the local keycloak instance
# If the --deployed flag is given, the script will use the deployed keycloak instance
# If no flag is given, the script will ask for the keycloak instance to use

import sys
import urllib
import json
import ssl
import pyperclip

from gql.transport.aiohttp import AIOHTTPTransport

KEYCLOAK_URL_DEPLOYED = "https://orange.informatik.uni-stuttgart.de/keycloak/realms/GITS/protocol/openid-connect/token"
KEYCLOAK_URL_LOCAL = "http://localhost:9009/realms/GITS/protocol/openid-connect/token"

use_local = None

if len(sys.argv) >= 3:
    user_name = sys.argv[1]
    user_password = sys.argv[2]

    if len(sys.argv) >= 4:
        if sys.argv[3] == "--local":
            use_local = True
        elif sys.argv[3] == "--deployed":
            use_local = False
        else:
            print("Invalid argument: " + sys.argv[3])
            sys.exit(1)
else:
    user_name = input("Please enter your user name: ")
    user_password = input("Please enter your password: ")

while use_local is None:
    user_answer = input("Use deployed keycloak instance? (y/n): ")
    if user_answer == "y":
        use_local = False
    elif user_answer == "n":
        use_local = True
    else:
        print("Invalid input.")

data = {
    "grant_type": "password",
    "client_id": "gits-frontend",
    "username": user_name,
    "password": user_password
}

url = KEYCLOAK_URL_LOCAL if use_local else KEYCLOAK_URL_DEPLOYED
data = urllib.parse.urlencode(data).encode("utf-8")
req = urllib.request.Request(url, data=data)

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

with urllib.request.urlopen(req, context=ctx) as response:
    user_token = json.loads(response.read())["access_token"]

if not user_token:
    print("Could not get user token.")
    sys.exit(1)

print("Your user token:")
print(user_token)

auth_header = "{ \"authorization\": \"" + user_token + "\" }"

# copy to clipboard

pyperclip.copy(auth_header)
print("Copied authorization header to clipboard.")
