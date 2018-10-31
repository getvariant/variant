mysql -v -u root -pvariant1 << EOF
  DROP DATABASE IF EXISTS variant;
  DROP USER IF EXISTS variant@localhost;
  CREATE DATABASE variant;
  CREATE USER variant@localhost IDENTIFIED BY 'variant';
  GRANT ALL on variant.* TO variant@localhost;
  \q
EOF
