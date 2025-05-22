#!/bin/bash

echo "Testing database name changes from tradingbotv2 to finmanapp"

# Check application.properties
echo "Checking application.properties..."
if grep -q "finmanapp" src/main/resources/application.properties; then
  echo "✅ application.properties uses finmanapp"
else
  echo "❌ application.properties might still be using tradingbotv2"
fi

# Check application-h2.properties
echo "Checking application-h2.properties..."
if grep -q "finmanapp" src/main/resources/application-h2.properties; then
  echo "✅ application-h2.properties uses finmanapp"
else
  echo "❌ application-h2.properties might still be using tradingbotv2"
fi

# Check SQL migration files
echo "Checking SQL migration files..."
if grep -q "FinManApp" src/main/resources/db/migration/V1__create_initial_schema.sql; then
  echo "✅ V1__create_initial_schema.sql uses FinManApp"
else
  echo "❌ V1__create_initial_schema.sql might still be using TradingBotV2"
fi

if grep -q "FinManApp" src/main/resources/db/migration/V2__seed_initial_data.sql; then
  echo "✅ V2__seed_initial_data.sql uses FinManApp"
else
  echo "❌ V2__seed_initial_data.sql might still be using TradingBotV2"
fi

# Check docker-compose.yml
echo "Checking docker-compose.yml..."
if grep -q "finmanapp" docker-compose.yml; then
  echo "✅ docker-compose.yml uses finmanapp"
else
  echo "❌ docker-compose.yml might still be using tradingbotv2"
fi

# Check shell scripts
echo "Checking shell scripts..."
if grep -q "finmanapp" start.sh; then
  echo "✅ start.sh uses finmanapp"
else
  echo "❌ start.sh might still be using tradingbotv2"
fi

if grep -q "finmanapp" fix-postgres.sh; then
  echo "✅ fix-postgres.sh uses finmanapp"
else
  echo "❌ fix-postgres.sh might still be using tradingbotv2"
fi

# Check documentation
echo "Checking documentation..."
if grep -q "finmanapp" README.md; then
  echo "✅ README.md uses finmanapp"
else
  echo "❌ README.md might still be using tradingbotv2"
fi

if grep -q "FinManApp" DATABASE.md; then
  echo "✅ DATABASE.md uses FinManApp"
else
  echo "❌ DATABASE.md might still be using TradingBotV2"
fi

echo ""
echo "Test completed. If all tests passed, the database name has been successfully updated."