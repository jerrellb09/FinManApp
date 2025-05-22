#!/bin/bash

# Array of controller files to modify
CONTROLLERS=(
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/AccountController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/AuthController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/BillController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/BudgetController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/CategoryController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/InsightController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/TransactionController.java"
  "/Users/jerrell/repos/FinManApp/src/main/java/com/jay/home/finmanapp/controller/UserController.java"
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