
# crudfy

#### PT:

Essa é uma ferramenta utilitaria para agilizar o desenvolvimento de APIs CRUD, utilizando spring-jpa. O projeto gerado tem toda a estrutura de controller, service, domain, repository e o arquivo pom.xml

1. **Gerando a API**  
   Utilizando uma requisição POST "/crud" com o body usando a seguinte estrutura:

  ```
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
            "type": "String"
          },
          {
            "name": "field2",
            "type": "Integer"
          },
          {
            "name": "field3",
            "type": "LocalDate"
          }
        ]
      }
    ]
}
  ```
- **path:** O caminho onde serão criados os arquivos. (É importante escapar as "\")
- **projectName:** O nome do projeto é usado como base para o nome dos pacotes, arquivos e variaveis
- **entities:**
  - name: Nome da entidade
  - fields: Além dos tipos primitivos, pode verificar outros tipos suportados em "src/main/resources/imports-mapping.json"

Obs: Ainda não é suportado tabelas utilizando chaves compostas

#### EN: