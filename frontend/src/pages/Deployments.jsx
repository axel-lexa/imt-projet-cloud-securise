import React from "react";
import { useParams } from "react-router-dom";

export default function Deployments() {
    const { id } = useParams();

    // Pour le mock, on pourrait afficher les steps détaillés d’un pipeline
    return (
        <div style={{ padding: "20px" }}>
            <h2>Détails du pipeline {id}</h2>
            <p>Ici on afficherait les étapes étape par étape et les logs.</p>
        </div>
    );
}
