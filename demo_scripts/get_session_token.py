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
import urllib.request
import urllib.parse
import json
import ssl
import pyperclip
import argparse

KEYCLOAK_URL_DEPLOYED = "https://orange.informatik.uni-stuttgart.de/keycloak/realms/GITS/protocol/openid-connect/token"
KEYCLOAK_URL_LOCAL = "http://localhost:9009/realms/GITS/protocol/openid-connect/token"

def get_auth_token(user_name: str = None, user_password: str = None, use_local: bool = None):
    # if no info provided, ask user interactively if local or deployed keycloak should be used
    while use_local is None:
        user_answer = input("Use deployed keycloak instance? (y/n): ")
        if user_answer == "y":
            use_local = False
        elif user_answer == "n":
            use_local = True
        else:
            print("Invalid input.")

    # if no info provided, ask user interactively for user name and password
    if user_name is None:
        user_name = input("Username: ")
    if user_password is None:
        user_password = input("Password: ")

    data = {
        "grant_type": "password",
        "client_id": "frontend",
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

    return user_token

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Get a session token for a user.")
    parser.add_argument("--local", action="store_true", dest="use_local", help="Use the local keycloak instance.")
    parser.add_argument("--deployed", action="store_false", dest="use_local", help="Use the deployed keycloak instance.")
    parser.add_argument("--username", help="Usernameo of the user to connect with.")
    parser.add_argument("--password", help="Password of the user to connect with.")
    parser.add_argument("--copy-to-clipboard", action="store_true", dest="copy_to_clipboard", help="Copy the authorization header to the clipboard.")

    args = parser.parse_args()

    user_token = get_auth_token(args.username, args.password, args.use_local)

    print("Your user token:")
    print(user_token)

    auth_header = "{ \"authorization\": \"" + user_token + "\" }"

    # copy to clipboard
    if(args.copy_to_clipboard):
        pyperclip.copy(auth_header)
        print("Copied authorization header to clipboard.")
