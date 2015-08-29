
psql -U postgres <<EOF
  drop database variant;
  drop user variant;
  create database variant;
  \connect variant
  create user variant password 'variant';
EOF

basedir=$(dirname $0)

export PGPASSWORD=variant
psql -U variant < $basedir/../src/main/resources/db/create-schema.sql
