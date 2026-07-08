# SystemStock

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-007396?logo=java)](https://www.oracle.com/java/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-11-CC092F?logo=jakartaee)](https://jakarta.ee/)
[![Servlets](https://img.shields.io/badge/Servlets-5.0-007396?logo=java)](https://jakarta.ee/specifications/servlet/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apache-maven)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![Gson](https://img.shields.io/badge/Gson-2.10-4285F4?logo=google)](https://github.com/google/gson)
[![BCrypt](https://img.shields.io/badge/BCrypt-0.4-222222?logo=hashicorp)](https://www.mindrot.org/projects/jBCrypt/)
[![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5)](https://developer.mozilla.org/en-US/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Tomcat](https://img.shields.io/badge/Tomcat-EE5F2E?logo=apachetomcat&logoColor=white)](https://tomcat.apache.org/)
[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?logo=githubactions)](https://github.com/features/actions)

## Visão Geral

**SystemStock** é um sistema de controle de estoque web desenvolvido em **Java Jakarta EE** com frontend em **HTML/CSS/JavaScript puro**. O projeto demonstra controle completo de inventário, solicitações automáticas de compra, autenticação e experiência de usuário moderna.

## Principais Funcionalidades

- Cadastro, edição e listagem de produtos
- Controle de movimentações de estoque (entrada/saída)
- Emissão automática de solicitações de compra quando o estoque fica abaixo do mínimo
- Filtro de solicitações por status e atualização de status via API
- Autenticação de usuário com hash seguro (`BCrypt`)
- Interface responsiva com painel de gerenciamento e cards estilizados

## Tecnologias Utilizadas

- Java 17 (compilação) e Java 21 no container/CI
- Jakarta EE 11 com Servlets e APIs Web
- Maven para build e dependências
- MySQL como banco de dados relacional
- JDBC para persistência de dados
- Gson para serialização JSON
- jBCrypt para segurança de senha
- HTML5, CSS3 e JavaScript puro para UI
- Docker + Tomcat para containerização
- GitHub Actions para build contínuo

## Arquitetura do Projeto

- `src/main/java`
  - `controller/` — endpoints HTTP e manipulação de requests
  - `dao/` — acesso a dados e lógica de persistência
  - `model/` — modelos de domínio
  - `util/` — validação, hashing e suporte de autenticação

- `src/main/webapp`
  - `pages/` — telas HTML da aplicação
  - `css/` — estilos e layout
  - `js/` — lógica de interface e chamadas AJAX
  - `META-INF/persistence.xml` — configuração de persistência

- `db/` — scripts de inicialização e migração do banco de dados
- `dockerfile` — build e deploy Docker para Tomcat
- `docker-compose.yml` — orquestração com MySQL e aplicação
- `github/ci-cd.yml` — pipeline de build no GitHub Actions

## Diferenciais Técnicos

- Emissão automática de solicitações de compra baseada no nível de estoque
- UI com gerenciamento de estados de carregamento, vazio e erro
- Filtro dinâmico de solicitações e atualização de status em tempo real
- Separação clara entre apresentação, serviço e persistência
- Containerização com Docker e integração contínua via GitHub Actions

## Como Executar

1. Configure as variáveis de ambiente do banco de dados:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASS`
   - `DB_HOST`
   - `MYSQL_DATABASE`
   - `MYSQL_ROOT_PASSWORD`
   - `DB_PORT_EXTERNAL`

2. Build com Maven:

```bash
cd C:/Projetos/SystemStock
./mvnw clean package
```

3. Build e execute com Docker:

```bash
docker compose up --build
```

4. Acesse a aplicação em:

```text
http://localhost:8080
```

---

`SystemStock` | Sistema de estoque completo com interface web responsiva e backend corporativo.