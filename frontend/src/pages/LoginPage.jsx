import React from 'react';
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Github} from "lucide-react";

export default function LoginPage() {

    const handleLogin = () => {
        // Redirection explicite vers le backend Spring Boot pour lancer le flux OAuth2
        // Le port 8080 est celui par défaut de Spring Boot
        window.location.href = "http://localhost:8081/oauth2/authorization/github";
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <Card className="w-[350px]">
                <CardHeader>
                    <CardTitle className="text-center">Connexion CI/CD</CardTitle>
                </CardHeader>
                <CardContent>
                    <p className="text-center text-gray-500 mb-6 text-sm">
                        Connectez-vous pour accéder au dashboard
                    </p>
                    <Button
                        className="w-full gap-2"
                        onClick={handleLogin}
                    >
                        <Github className="w-4 h-4"/>
                        Se connecter avec GitHub
                    </Button>
                </CardContent>
            </Card>
        </div>
    );
}