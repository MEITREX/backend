# This script allows you to get a session token for a user
# Usage: python get_session_token.py <user_name> <user_password>
# If no arguments are given, the user will be prompted for the user name and password
# An authorization header will be copied to the clipboard
# The keycloak url must be changed if the keycloak server is not running on localhost:9009

import sys
import urllib
import json

from gql.transport.aiohttp import AIOHTTPTransport

KEYCLOAK_URL = "http://localhost:9009/realms/GITS/protocol/openid-connect/token"

if len(sys.argv) == 3:
    user_name = sys.argv[1]
    user_password = sys.argv[2]
else:
    user_name = input("Please enter your user name: ")
    user_password = input("Please enter your password: ")

data = {
    "grant_type": "password",
    "client_id": "gits-frontend",
    "username": user_name,
    "password": user_password
}

data = urllib.parse.urlencode(data).encode("utf-8")
req = urllib.request.Request(KEYCLOAK_URL, data=data)
with urllib.request.urlopen(req) as response:
    user_token = json.loads(response.read())["access_token"]

if not user_token:
    print("Could not get user token.")
    sys.exit(1)

print("Your user token:")
print(user_token)

auth_header = "{ \"authorization\": \"" + user_token + "\" }"

# copy to clipboard
import pyperclip

pyperclip.copy(auth_header)
print("Copied authorization header to clipboard.")
