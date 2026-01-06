import axios from 'axios';

// L'URL de votre Backend Spring Boot
const API_URL = 'http://localhost:8080/api/pipelines';

// 1. Récupérer les pipelines (GET)
export const getPipelines = async () => {
    try {
        const response = await axios.get(API_URL);
        return response.data; // Retourne le tableau JSON envoyé par Java
    } catch (error) {
        console.error("Erreur lors de la récupération des pipelines", error);
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
        // Le backend attend une String brute pour l'URL, on configure le header text/plain
        const response = await axios.post(`${API_URL}/run`, repoUrl, {
            headers: {'Content-Type': 'text/plain'}
        });
        return response.data;
    } catch (error) {
        console.error("Erreur lors du déclenchement", error);
        throw error;
    }
};

// (Optionnel) Pour les utilisateurs, si vous n'avez pas fait le Controller Java correspondant,
// gardez le mock ou renvoyez un tableau vide pour ne pas faire planter le front.
export const getUsers = async () => {
    return [];
};

export const updateUserRole = async (userId, newRole) => {
    console.warn("API Utilisateurs non implémentée côté Backend");
    return null;
};