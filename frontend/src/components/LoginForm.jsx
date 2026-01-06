// src/components/login-form.jsx
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Github } from "lucide-react"

export function LoginForm({ className, ...props }) {

    const handleGithubLogin = () => {
        // Ici, tu redirigeras vers ton backend qui gère l'OAuth2 GitHub
        console.log("Redirection vers l'authentification GitHub...");
        window.location.href = "http://localhost:8080/oauth2/authorization/github";
    };

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Bienvenue</CardTitle>
                    <CardDescription>
                        Connectez-vous avec votre compte GitHub pour gérer vos pipelines.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="grid gap-6">
                        <div className="flex flex-col gap-4">
                            <Button variant="outline" className="w-full" onClick={handleGithubLogin}>
                                <Github className="mr-2 h-4 w-4" />
                                Continuer avec GitHub
                            </Button>
                        </div>
                        <div className="relative text-center text-sm after:absolute after:inset-0 after:top-1/2 after:z-0 after:flex after:items-center after:border-t after:border-border">
              <span className="relative z-10 bg-background px-2 text-muted-foreground">
                Ou via email
              </span>
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="email">Email</Label>
                            <Input id="email" type="email" placeholder="nom@imt.fr" required />
                        </div>
                        <div className="grid gap-2">
                            <div className="flex items-center">
                                <Label htmlFor="password">Mot de passe</Label>
                            </div>
                            <Input id="password" type="password" required />
                        </div>
                        <Button type="submit" className="w-full">
                            Se connecter
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}