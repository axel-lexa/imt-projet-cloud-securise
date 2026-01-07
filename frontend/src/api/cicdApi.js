import axios from 'axios';

// Détection automatique de l'URL de base
// Si on est en développement (Vite server), on pointe vers 8081
// Si on est en production (servi par Spring), on utilise l'URL relative
const API_URL = import.meta.env.DEV
    ? 'http://localhost:8081/api/pipelines'
    : '/api/pipelines';

// Configuration globale d'Axios pour inclure les cookies (JSESSIONID)
axios.defaults.withCredentials = true;

// 1. Récupérer les pipelines (GET)
export const getPipelines = async () => {
    try {
        const response = await axios.get(API_URL);
        return response.data;
    } catch (error) {
        console.error("Erreur lors de la récupération des pipelines", error);
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            // Redirection gérée par le composant ou le routeur idéalement
            // window.location.href = "/login";
        }
        return [];
    }
};

export const getPipelineById = async (id) => {
    try {
        const response = await axios.get(`${API_URL}/${id}`);
        return response.data;
    } catch (error) {
        console.error("Erreur pipeline detail", error);
        return null;
    }
};

// 2. Déclencher un pipeline (POST)
export const triggerPipeline = async (repoUrl) => {
    try {
        const response = await axios.post(`${API_URL}/run`, repoUrl, {
            headers: {'Content-Type': 'text/plain'}
        });
        return response.data;
    } catch (error) {
        console.error("Erreur lors du déclenchement", error);
        throw error;
    }
};

export const getUsers = async () => {
    return [];
};

export const updateUserRole = async (userId, newRole) => {
    console.warn("API Utilisateurs non implémentée côté Backend");
    return null;
};

// Vérifie si l'utilisateur est authentifié
export const checkAuthStatus = async () => {
    try {
        // On tente d'accéder à une ressource protégée simple
        // Si on reçoit une 200, c'est qu'on est loggé. Si 401/403, non.
        // Vous pouvez aussi créer un endpoint dédié @GetMapping("/api/me") côté Spring
        await axios.get(`${API_URL}`);
        return true;
    } catch (error) {
        return false;
    }
};
