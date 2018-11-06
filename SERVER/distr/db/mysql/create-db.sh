mysql -v -u root -pvariant1 << EOF
  SET GLOBAL time_zone = '-08:00';
  DROP DATABASE IF EXISTS variant;
  DROP USER IF EXISTS variant@localhost;
  CREATE DATABASE variant;
  CREATE USER variant@localhost IDENTIFIED BY 'variant';
  GRANT ALL on variant.* TO variant@localhost;
  \q
EOF
