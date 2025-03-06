# Changes Made to TradingBotV2

## Database Migration from H2 to PostgreSQL

### Summary
We've migrated the application from using an in-memory H2 database to a persistent PostgreSQL database, while maintaining H2 as a fallback option for development or when Docker isn't available.

### Changes

1. **Database Configuration**
   - Added PostgreSQL configuration in `application.properties`
   - Created separate `application-h2.properties` for H2 fallback
   - Added connection pool settings for better performance

2. **Data Seeding**
   - Created `DataSeederConfig` for programmatic data seeding
   - Added sample data for users, accounts, budgets, transactions, and bills
   - Included error handling for robust seeding

3. **Development Tools**
   - Added Docker Compose file for PostgreSQL
   - Created startup script to automatically choose the best database option
   - Added database initialization checks

4. **Documentation**
   - Updated README with instructions for both database options
   - Added detailed API documentation
   - Documented sample data

### Benefits

1. **Persistence**: Data is now stored persistently across application restarts
2. **Performance**: PostgreSQL offers better performance for larger datasets
3. **Development Flexibility**: Developers can choose between H2 and PostgreSQL
4. **Realistic Testing**: Sample data that closely resembles real-world usage

### How to Run

For PostgreSQL (persistent data):
```bash
docker-compose up -d
./mvnw spring-boot:run
```

For H2 (in-memory, no persistence):
```bash
./mvnw spring-boot:run -Dspring.profiles.active=h2
```

Automatic detection:
```bash
./start.sh
```