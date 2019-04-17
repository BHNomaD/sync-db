#!/usr/bin/env bash
rm -r build
gradle clean assemble
tar -xf build/distributions/*.tar -C build/distributions/
./build/distributions/sync-db-1.0-SNAPSHOT/bin/sync-db
