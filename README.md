[![build](https://github.com/oliverdozsa/galactic.host/actions/workflows/scala.yml/badge.svg)](https://github.com/oliverdozsa/galactic.host/actions/workflows/scala.yml) [![Maintainability](https://api.codeclimate.com/v1/badges/93e42d6218e93df2e2a6/maintainability)](https://codeclimate.com/github/oliverdozsa/galactic.host/maintainability) [![Test Coverage](https://api.codeclimate.com/v1/badges/93e42d6218e93df2e2a6/test_coverage)](https://codeclimate.com/github/oliverdozsa/galactic.host/test_coverage) 

<img src="docs/logo.png" alt="drawing"/>

# Galactic Host
This is the server part of [Galactic Pub](https://galactic.pub). Includes the following sub-projects.

## Voting
A privacy first voting platform powered by blockchain technology. Inspired by [stellot](https://github.com/stanbar/stellot). 
Work-in-progress.
### What are the differences?
One of the main goals would be to support multiple blockchains (currently only stellar).
### How does it work?
#### Casting a vote
It's based on [blind signatures](https://en.wikipedia.org/wiki/Blind_signature#Blind_RSA_signatures).
1. The voter first authenticates with server. In order to get a vote token anonymously, it creates a concealed request, which contains information
about the voter's account where the vote token should be delivered. 
2. The concealed request will be sent to the server for signing
3. From the signature on the concealed request voter creates the signature for the revealed request.
4. Voter becomes anonymous, and sends the revealed signature, and request to the server.
5. The server checks the revealed signature, so that it knows the anonymous voter is a participant of the voting in question.
6. Server sends back the transaction so that voter can obtain the vote token.
![cast vote](./docs/cast-vote-seq.svg)
### Documentation
The API doc is available [here](https://oliverdozsa.github.io/galactic.host).
