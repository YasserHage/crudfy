
# Java CRUD Project Generator (Crudfy)

This is a utility tool designed to accelerate the development of CRUD APIs using [**Spring Data**](https://spring.io/projects/spring-data) for several Databases.  
It uses [**JavaParser**](https://javaparser.org/) to dynamically generate Java classes and project structure based on user input.

The generated project includes:

- Controller  
- Service  
- Domain (Entities and DTOs)  
- Repository  
- `pom.xml` file (Maven configuration)

---

## 🔧 How to Generate a CRUD API

Make a `POST` request to the `/crud` endpoint with a JSON body in the following format:

  ``` json
{
    "path": "C:\\My\\Path",
    "projectName": "projectName",
    "database": "MONGODB || MYSQL || ELASTICSEARCH",
    "entities": [
      {
        "name": "entityName",
        "fields": [
          {
            "name": "id",
            "type": "String",
            "isId": true
          },
          {
            "name": "field1",
            "type": "List<String>"
          },
          {
            "name": "field2",
            "type": "Integer"
          },
          {
            "name": "field3",
            "type": "OtherEntity",
            "isSubEntity" : true
          },
          {
            "name": "field4",
            "type": "LocalDate"
          }
        ]
      },
      {
        "name": "otherEntity",
        "fields": [
          {
            "name": "field",
            "type": "String"
          }
        ]
      }
    ]
}
  ```
### 📝 Parameters

- **`path`**:  
  The absolute path where the project will be created. Make sure to escape backslashes (`\\`) on Windows.

- **`projectName`**:  
  Used as the base for package names, class names, and variable names.

- **`database`**:  
  The target database. Supported options: `MONGODB`, `MYSQL`, `ELASTICSEARCH`.

- **`entities`**:  
  List of entity definitions, each with:
  - `name`: Name of the entity
  - `fields`: Field definitions including:
    - `name`: Field name  
    - `type`: Data type (primitives and supported custom types)
    - `isSubEntity`: Marks the field as a Sub Entity (should be defined in the entity array latter)
    - `isId` (optional): Marks the field as the ID

💡 **Note**: For supported field types beyond primitives, check the mappings in  
`src/main/resources/imports-mapping.json`.

---

## ⚠️ Limitations

- Currently **does not** support composite primary keys.

---

## 📦 Technologies Used

- Java 11+  
- Spring Boot  
- Spring Data JPA, MongoDB and Elasticsearch  
- JavaParser
