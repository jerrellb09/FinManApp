#!/bin/bash

# Array of controller files to modify
CONTROLLERS=(
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/AccountController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/AuthController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/BillController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/BudgetController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/CategoryController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/InsightController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/TransactionController.java"
  "/Users/jerrell/repos/TradingbotV2/src/main/java/com/jay/home/tradingbotv2/controller/UserController.java"
)

# Process each controller file
for file in "${CONTROLLERS[@]}"; do
  if [ -f "$file" ]; then
    echo "Processing $file"
    # Remove @CrossOrigin annotations using sed
    sed -i '' -E 's/@CrossOrigin\(origins = "\*", maxAge = 3600\)//' "$file"
    echo "- Removed @CrossOrigin annotation"
  else
    echo "File not found: $file"
  fi
done

echo "Done removing @CrossOrigin annotations from controllers."