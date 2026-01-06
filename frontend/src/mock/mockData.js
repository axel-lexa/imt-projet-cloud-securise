// src/mock/mockData.js
export const pipelines = [
    { id: "P-101", name: "Frontend Production", status: "SUCCESS", trigger: "Manual", date: "2024-05-20 14:30" },
    { id: "P-102", name: "API Gateway", status: "RUNNING", trigger: "GitHub Push", date: "2024-05-20 15:45" },
    { id: "P-103", name: "Auth Service", status: "FAILED", trigger: "GitLab CI", date: "2024-05-19 09:12" },
    { id: "P-104", name: "Database Migrator", status: "SUCCESS", trigger: "Schedule", date: "2024-05-18 22:00" },
    { id: "P-105", name: "Mobile App Build", status: "PENDING", trigger: "Manual", date: "2024-05-20 16:00" },
];

export const users = [
    { id: 1, name: "Romain Jacovetti", email: "romain@company.com", role: "ADMIN", status: "Active" },
    { id: 2, name: "Axel Elias", email: "axel@company.com", role: "DEVELOPER", status: "Active" },
    { id: 3, name: "Thomas Dubot", email: "thomas@company.com", role: "RETIRED", status: "Inactive" },
    { id: 4, name: "Théo Lebiez", email: "theo@company.com", role: "FUTUR BRETON", status: "Active" },
];