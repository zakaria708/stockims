# StockIMS — Inventory Management System

**StockIMS** is a web-based inventory management application developed using Java, Spring Boot, MySQL, Thymeleaf, HTML, CSS, and JavaScript.

The application provides a centralized interface for managing products, monitoring stock levels, recording inventory movements, and organizing inventory data securely.

Developed as an academic software project at **ESITH (École Supérieure des Industries du Textile et de l'Habillement)**.

## 1. Overview

StockIMS simplifies everyday inventory management by allowing users to register articles, monitor stock availability, and keep a detailed history of stock movements.

The application features a responsive dashboard, interactive article management, import/export functionality, and account-based inventory separation.

### Main objectives

- Simplify inventory tracking and organization.
- Reduce manual stock management errors.
- Monitor inventory levels and identify stock alerts.
- Maintain accurate records of stock entries and exits.
- Provide a clean and intuitive user experience.
- Protect user information and separate inventory data between accounts.

## 2. Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| Spring Boot 4.1.1 | Application framework |
| Spring MVC | Web controllers and routing |
| Spring Security | Authentication and access control |
| Spring Data JPA | Database persistence |
| Hibernate | Object-relational mapping |
| MySQL | Relational database |
| Thymeleaf | Server-side HTML templates |
| HTML5 | Application structure |
| CSS3 | Responsive styling |
| JavaScript | Interactive interface |
| Apache POI | Excel file processing |
| Apache Commons CSV | CSV processing |
| Maven | Build and dependency management |
| JUnit | Automated testing |

## 3. Features

### Dashboard

The dashboard provides an overview of inventory activity, including:

- Total number of registered articles.
- Total quantity of units in stock.
- Number of low-stock articles.
- Number of articles classified as depleted.
- Visual stock distribution.
- Quick access to inventory management features.

### Article Management

Users can:

- Create new articles.
- Edit existing article information.
- Delete articles from their inventory.
- Assign references and categories.
- Define initial quantities and alert thresholds.
- Search and filter articles.
- View current stock status.

### Stock Movement Management

StockIMS supports two types of inventory movements:

**Stock Entry:** Register incoming quantities and update available inventory.

**Stock Exit:** Register outgoing quantities while preventing withdrawals exceeding the available stock.

Inventory movements are recorded to maintain traceability.

### Stock Status Monitoring

The application classifies articles into three categories:

| Condition | Status |
|---|---|
| Stock greater than alert threshold | Disponible |
| Stock positive and below alert threshold | Faible |
| Stock equal to threshold or equal to zero | Épuisé |

This project uses a customized classification rule in which an article is considered **Épuisé** when its stock reaches the configured alert threshold.

For example, an article with 8 units in stock and an alert threshold of 8 will be classified as Épuisé.

This classification does not necessarily mean that the physical quantity is zero.

### File Import and Export

StockIMS supports bulk article imports using:

- CSV (`.csv`)
- Excel (`.xlsx`, `.xls`)
- Text (`.txt`)

Imported data can include article references, descriptions, categories, alert thresholds, and initial quantities.

The application also supports exporting inventory information to CSV.

Import files are limited to 5 MB and are validated before processing.

### User Authentication

The application includes:

- User registration.
- Secure login and logout.
- Password hashing using BCrypt.
- Spring Security authentication.
- CSRF protection.
- Separate inventory data for each account.
- Server-side access controls for inventory operations.

Each registered account maintains its own articles, quantities, movements, and inventory statistics.

## 4. JavaScript Enhancements

JavaScript improves usability and provides interactive behavior without replacing server-side business logic.

Implemented features include:

- Real-time stock status previews.
- Dynamic feedback when entering stock quantities and thresholds.
- File format and file size validation before import.
- Confirmation dialogs before article deletion.
- Responsive mobile navigation.
- Interactive import functionality.

Critical operations remain validated by the Spring Boot backend.

## 5. Project Structure

```text
stockims/
|
|-- README.md
|-- stock-ims/
    |
    |-- .mvn/
    |-- mvnw
    |-- mvnw.cmd
    |-- pom.xml
    |
    |-- src/
        |
        |-- main/
        |   |
        |   |-- java/ma/esith/stockims/
        |   |   |-- config/
        |   |   |-- model/
        |   |   |-- repository/
        |   |   |-- service/
        |   |   |-- web/
        |   |   |-- StockImsApplication.java
        |   |
        |   |-- resources/
        |       |-- static/
        |       |   |-- css/
        |       |   |-- js/
        |       |   |-- exemples/
        |       |
        |       |-- templates/
        |           |-- articles/
        |           |-- mouvements/
        |           |-- stock/
        |           |-- fragments/
        |
        |-- test/
            |-- java/
            |-- resources/
```

## 6. Installation and Setup

### Prerequisites

Ensure the following tools are available:

- Java Development Kit (JDK) 21
- MySQL Server
- Git
- An IDE such as IntelliJ IDEA or Visual Studio Code

Maven does not need to be installed separately because the project includes Maven Wrapper.

### Clone the Repository

```bash
git clone https://github.com/zakaria708/stockims.git
```

Navigate to the application directory:

```bash
cd stockims/stock-ims
```

### Database Configuration

Create a MySQL database named:

```sql
CREATE DATABASE IF NOT EXISTS stockims_db;
```

Configure the following environment variables to match your MySQL installation:

```text
DB_URL=jdbc:mysql://localhost:3306/stockims_db
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
```

On Windows PowerShell, they can be configured for the current terminal session:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/stockims_db"
$env:DB_USERNAME="your_mysql_username"
$env:DB_PASSWORD="your_mysql_password"
```

Never commit database passwords or other sensitive credentials to GitHub.

### Run the Application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

Once the application starts, open:

**http://localhost:8080**

Create an account or sign in to access the inventory management interface.

## 7. Import File Example

Example of a supported CSV file:

```csv
reference;designation;categorie;seuilAlerte;stockInitial
TIS-001;Rouleau de coton blanc;Textile;10;50
TIS-002;Fil polyester noir;Fil;20;100
TIS-003;Piece mecanique metier;Maintenance;8;8
```

Required columns:

- `reference`
- `designation`

Optional columns:

- `categorie`
- `seuilAlerte`
- `stockInitial`

For additional details, see [Import Documentation](stock-ims/IMPORT_FICHIERS.md).

## 8. Testing

The project includes automated tests covering stock classification, inventory services, account separation, and other application behavior.

To run the tests on Windows:

```powershell
.\mvnw.cmd test
```

Tests use an H2 test database instead of modifying the production MySQL inventory.

## 9. Security Considerations

StockIMS uses several security measures:

- Authentication through Spring Security.
- Password hashing using BCrypt.
- CSRF protection for applicable operations.
- Separation of inventory data by authenticated account.
- Server-side validation of article and movement operations.
- Access restrictions preventing users from managing another account's articles.
- File upload validation and size restrictions.

For deployment, credentials should be configured securely and default demonstration passwords should be changed.

## 10. Future Improvements

Potential improvements include:

- Advanced inventory analytics and reporting.
- Interactive charts and statistics.
- Supplier and purchase order management.
- Product barcode and QR code support.
- More advanced filtering and search.
- Automated low-stock notifications.
- Improved mobile accessibility.
- Containerized deployment using Docker.
- CI/CD integration with GitHub Actions.

These features are planned improvements and are not part of the current implementation.

## 11. Project Documentation

Additional technical documentation is available in the repository:

- [Import and Export](stock-ims/IMPORT_FICHIERS.md)
- [Account-Based Inventory Management](stock-ims/LISEZ_MOI_COMPTES_SEPARES.md)
- [Stock Status and JavaScript Changes](stock-ims/README_MODIFICATIONS_STOCK_ET_JS.md)
- [Application Improvements](stock-ims/README_AMELIORATIONS.md)
- [Validation Notes](stock-ims/VALIDATION.md)

## 12. Author

**GitHub:** [zakaria708](https://github.com/zakaria708) [Hiba1324](https://github.com/Hiba1324) 

**Institution:** ESITH — École Supérieure des Industries du Textile et de l'Habillement

**Project:** StockIMS — Inventory Management System

---

StockIMS is an academic project focused on applying software engineering concepts to real-world inventory management requirements.
