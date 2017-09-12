# General
QR-auth v.0.5.0, in development.

# Purpose
Provide an interface for users to authenticate using solely a smartphone and QR-Codes. Authentication can be multi-devices: browser (PC, tablet), smartphone (app).

Get to the principles to understand how easy your user signs in and logs in; but much better, he is able to perform it with the smartphone's two factor authentication !
* User owns it phone
* User knows its PIN Code.

# Principle
Using a QR-authentication to log in without credentials to a system. Compatible Node.js code.

Thing is as follows:
Assuming the company MyCompany is getting a new user: MyUser, who has at least a smartphone.
Assumng then that MyCompany has a website and an app.

## Sign in
1. MyUser (Browser) connects to the login URL `https://mycompany.com/sign-in`. He is prompted a QR-Code to log in.
2. MyUser(Android) has downloaded MyCompany App, opens it. The App prompts its camera interface, and MyUser shots the QR-Code. Instantly, its browser and/or app are synced and a sign-in form appears (note: **all** the fields in the sign-in are **optional**, that is to say, it could even not be a sign-in form !)
3. MyUser(PC or Android) fills in datas and an account is created, he is logged in in its both devices.

## Log in
1. MyUser's app is always connected, insofar as its account is bound with its phone number. No authentication then, except unlocking its own phone.
2. MyUser connects to the website `https://mycompany.com/login/`. the websites simply prompts him a QR-Code.
3. MyUser unlocks his phone, open MyCompany's app, goes to a "browser login" tab. A camera interface is prompted, and MyUser shots the QR-Code.
4. Instantly, the browser shows MyUser personal space: he is logged in.

# Core authentication scheme
## Sign in
1. Access to Sign in URL through HTTPS mandatory, auto generating PSK (short) appended in the URL (WARNING: resource exhaustion risk: Mitigate by anti-DDoS system). E.g. `http://mycompany.com/authenticate?id=09df8d7af519e`. Turns into QR-Code. Submits it.
2. Android App gets URL and submits it with WebSockets Message, generated from salted hash of phone number.
```javascript
"09df8d7af519e": {
    "PhoneIdHash": "6d96270004515a0486bb7f76196a72b40c55a47f",
    "salt": "HELLOWORLD"
}
```
Note that browser, server and client share the same WebSocket room.

3. Server binds PSK to PhoneIdHash, the browser session now saves the information to the PhoneIdHash. Somewhere in the database, we get: 
```javascript
[
    "6d96270004515a0486bb7f76196a72b40c55a47f": {
        "name": "Georges Melies"
    }
]
```

## Log in
1. Access to Login URL through HTTPS mandatory, auto generating PSK (short) appended in the URL (WARNING: resource exhaustion risk: Mitigate by anti-DDoS system). E.g. `http://mycompany.com/authenticatelogin?id=09df8d7af519e`. Turns into QR-Code. Submits it.
2. Android App gets URL and submits it with WebSockets Message, generated from salted hash of phone number.
```javascript
{
"PhoneIdHash" : "6d96270004515a0486bb7f76196a72b40c55a47f"
"salt" : "HELLOWORLD"
}
```
3. Server binds PSK to PhoneIdHash, the browser session queries database with Unique ID PhoneIdHash and loads User information.

# Roadmap
## Proof of Concept
- [X] Having a running instance of Node.js and dependencies
- [X] Having Websockets running on a small example
- [X] **TAG 0.1.0** Having a QR-Code generator
- [X] **TAG 0.2.0** Using external URL, disclose webpage and unique ID i another device
- [X] **TAG 0.3.0** Using QR-Code through phone's browser, simulate the user stori described in "Principle" section
- [X] **TAG 0.4.0** Having a dummy Android App with a QR-Code reader.
- [X] **TAG 0.5.0** Using App's QR-code reader, simulate the user story.
- [ ] **TAG 0.6.0** Code check with Unique ID handshakes.
 
## Packaging
- [ ] **TAG 0.7.0** Perform a proper Node.js module
- [ ] **TAG 0.8.0** Perform a proper Android module
- [ ] **TAG 0.9.0-1.0.0-beta** Making installation documentation
- [ ] **TAG 1.0.0-alpha** Perform test end-to-end
- [ ] **TAG 1.0.0** Release

