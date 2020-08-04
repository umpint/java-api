# umpint.com - Java Example API

Java API example to umpint.com document signing service.
This could be used in production as is reasonable fast.
But also can be used as sample code to embedd in you own application

## Getting Started

Just clone this project. Then can run the application to sign just one document or send signature for every file in a directory. Send every file in a directory is much faster as only one message is sent to the umpint servers. 

### Prerequisites

This application should compile with any recent version of mvn and Java (1.8+).

Only other requirements are that you need access to umpint.com over the internet to send the data. And you need access to the private key of the certificate of the domain you want to sign for.

### Installing

```
git clone https://github.com/umpint/java-api.git
cd java-api
mvn package
```

### Running

We have at dummybank.co.uk a dummy site you can use for testing. Since we only use if for testing we allow you to download the Certificate Private Key. Nobody should normally every allow this - as makes https pointless.

You can download from: dummybank.co.uk/notnormallypublic/privkey.pem 

Note this file changes every few months so you may have to download again if you start getting certificate errors.

Place this file in the bash-api directory.

To sign an individual file run:
```
wget https://dummybank.co.uk/notnormallypublic/privkey.pem
# 1st edit the file testfile.sh and make random change to it so it is unique
java -jar target/java-api-1.0-SNAPSHOT.jar dummybank.co.uk ./privkey.pem testfile.txt
```

you should then see output like this:

```
umpint java signer starting
2c261428d6b4fe81cc45d09565b9a6b58c2307c6da61f8b4026404bb0cfa1e2c
VaZaYMHtf7o6J9C8fbKHRk0l1BLMauGzmqdsBDQmcbMW34xmmcrjQQvcnx1rftci4W3slVoRsSu4~Xe8l7A8~DPuDvIp56A2EWMaxA~pImdnTo-qnPJwldoH6H36ugqwMg5FkSOv3jwY1NXD0rFRR1K2ZZbiPibq2or8ic11yqGgHNnxiIaYUOXOWOWPeJpIqSEcgHgZ3W3s7196eaQyzTrYzRmbK-UuaZY-WELySNXdfkyWC0U77YXg31xdrm9ZSDy9IpIo6LPN0BpZJ2V~SDB4nq3Dq3JA12UrOgFAo9C4O-RqobI2JuPXJmd9GalE~SQ1ekKrqTm3TXVlDOmV1A__
signed OK dummybank.co.uk 2c261428d6b4fe81cc45d09565b9a6b58c2307c6da61f8b4026404bb0cfa1e2c VaZaYMHtf7o6J9C8fbKHRk0l1BLMauGzmqdsBDQmcbMW34xmmcrjQQvcnx1rftci4W3slVoRsSu4/Xe8l7A8/DPuDvIp56A2EWMaxA/pImdnTo+qnPJwldoH6H36ugqwMg5FkSOv3jwY1NXD0rFRR1K2ZZbiPibq2or8ic11yqGgHNnxiIaYUOXOWOWPeJpIqSEcgHgZ3W3s7196eaQyzTrYzRmbK+UuaZY+WELySNXdfkyWC0U77YXg31xdrm9ZSDy9IpIo6LPN0BpZJ2V/SDB4nq3Dq3JA12UrOgFAo9C4O+RqobI2JuPXJmd9GalE/SQ1ekKrqTm3TXVlDOmV1A==
```

The "signed OK" signifies that the signature was correct and it was a new file.

If instead you see "duplicate already in db" this just means that you probably did not change the "testfile.txt" or have upload the file twice and so we already had a signature in the database. To fix this issue just change the file in some way.

When the 3rd parameter is a directory all the files will be signed in one call to the umpint.com API. Also results will be returned as a JSON array - see: https://github.com/umpint/rest-api/blob/master/batch_sign.md


## Contributing

Please just create a pull request and we will review and merge.

## Authors

* **Robin Owens** - *Initial work* - [Umpint](https://github.com/Umpint)

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE-2.0.txt](LICENSE-2.0.txt) file for details

## Acknowledgments

* People who wrote OpenSSL!
