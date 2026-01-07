# CloudSec CI/CD Dashboard

This project is a secure CI/CD dashboard application that allows you to trigger pipelines, monitor deployments, and manage users. It is built with a Spring Boot backend and a React frontend, packaged together in a Docker container.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

*   **Docker** and **Docker Compose**
*   **Git**
*   An SSH key pair for accessing your deployment VM (if applicable)
*   A GitHub OAuth App (for authentication)

## Setup Instructions for New Users

To run this application on your machine, follow these steps:

### 1. Clone the Repository

```bash
git clone <repository-url>
cd imt-projet-cloud-securise
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory of the project based on the provided `.env.example`.

```bash
cp .env.example .env
```

Open the `.env` file and fill in the required values:

*   `GITHUB_CLIENT_ID ` = `Ov23liEK5kJ4XVXWwO49`
*   `GITHUB_CLIENT_SECRET ` = `6a367badc8860be5c450e0270e775f7b78b5e629`
*   `VM_PRIVATE_KEY_PATH`: The absolute path to your private SSH key (e.g., `/Users/yourname/.ssh/id_rsa` or `C:\Users\yourname\.ssh\id_rsa`).
    *   *Note: Ensure this key has access to the target deployment VM.*

### 3. Run the Application

Start the entire stack (App, Database, SonarQube) using Docker Compose:

```bash
docker-compose up --build -d
```

This command will:
*   Build the frontend (React/Vite).
*   Build the backend (Spring Boot).
*   Package everything into a Docker image.
*   Start the PostgreSQL database and SonarQube.

### 4. Access the Application

Once the containers are running (this may take a few minutes for the first build):

*   **Dashboard**: Open [http://localhost:8081/login](http://localhost:8081) in your browser.
*   **SonarQube**: Open [http://localhost:9000](http://localhost:9000) (Login: `admin` / Password: `admin` or `admin123`).

## Troubleshooting

*   **Database Reset**: If you need to wipe the database and start fresh, run:
    ```bash
    docker-compose down -v
    docker-compose up -d
    ```

*   **SSH Key Issues**: Ensure the path in `.env` is correct and absolute. On Windows, you might need to use forward slashes `/` or double backslashes `\\`.

## Architecture

*   **Frontend**: React + Vite + Tailwind CSS
*   **Backend**: Spring Boot 3 + Spring Security (OAuth2)
*   **Database**: PostgreSQL
*   **Quality**: SonarQube
*   **Deployment**: Docker Compose
