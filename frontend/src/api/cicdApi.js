import { pipelines, users } from "../mock/mockData";

// Simule un call API pour récupérer les pipelines
export const getPipelines = async () => {
    return new Promise(resolve => {
        setTimeout(() => resolve(pipelines), 500);
    });
};

// Simule déclenchement d’un pipeline
export const triggerPipeline = async () => {
    return new Promise(resolve => {
        setTimeout(() => resolve({ message: "Pipeline triggered!" }), 500);
    });
};

// Récupère les utilisateurs
export const getUsers = async () => {
    return new Promise(resolve => {
        setTimeout(() => resolve(users), 500);
    });
};

// Met à jour le rôle d’un utilisateur
export const updateUserRole = async (userId, newRole) => {
    return new Promise(resolve => {
        const user = users.find(u => u.id === userId);
        if(user) user.role = newRole;
        setTimeout(() => resolve(user), 300);
    });
};
