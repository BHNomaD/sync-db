# README #

Application for synchronizing oracle-database with different databases

### What is this repository for? ###

* In the PostConstruct of SyncManager EVENT_LOG table will be created.
* List of tables that should be synced will be taken from the properties file (dbXX.sync-table-list)
* Then for each table that needed to be synced INSERT, DELETE and update trigger will be created

### How do I get set up? ###

* Set valid configaration to the application.yml file
* sh starup.sh

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Sohaib Reza
* Shariful Islam

###### Last updated: 23 APR 2019 03:26PM 