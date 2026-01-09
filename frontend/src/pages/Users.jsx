import React, {useEffect, useState} from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import {getUsers, updateUserRole} from "../api/cicdApi";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "../components/ui/table.jsx";
import {Badge} from "../components/ui/badge.jsx";
import {Button} from "../components/ui/button.jsx";
import {Card, CardContent, CardHeader, CardTitle} from "../components/ui/card.jsx";
import {Avatar, AvatarFallback, AvatarImage} from "../components/ui/avatar.jsx";
import {Loader2, ShieldAlert, ShieldCheck} from "lucide-react";

export default function Users() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    // Charge les utilisateurs au montage du composant
    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        const data = await getUsers();
        setUsers(data);
        setLoading(false);
    };

    // Fonction pour changer le rôle (Toggle simple DEV <-> ADMIN)
    const handleToggleRole = async (user) => {
        const newRole = user.role === "ADMIN" ? "DEV" : "ADMIN";
        await updateUserRole(user.id, newRole);
        loadUsers(); // Recharge la liste pour voir le changement
    };

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar/>
            <div className="flex-1 flex flex-col">
                <Topbar title="Gestion des Utilisateurs"/>

                <main className="p-6 max-w-6xl mx-auto w-full">
                    <Card>
                        <CardHeader>
                            <CardTitle>Équipe ({users.length})</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {loading ? (
                                <div className="flex justify-center p-8">
                                    <Loader2 className="animate-spin text-blue-600"/>
                                </div>
                            ) : (
                                <Table>
                                    <TableHeader>
                                        <TableRow>
                                            <TableHead className="w-[100px]">Avatar</TableHead>
                                            <TableHead>Nom / Pseudo</TableHead>
                                            <TableHead>Email / Identifiant</TableHead>
                                            <TableHead>Rôle</TableHead>
                                            <TableHead className="text-right">Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {users.map((user) => (
                                            <TableRow key={user.id}>
                                                <TableCell>
                                                    <Avatar>
                                                        {/* On génère un avatar auto si pas d'image */}
                                                        <AvatarImage src={`https://github.com/${user.name}.png`}/>
                                                        <AvatarFallback>{user.name.substring(0, 2).toUpperCase()}</AvatarFallback>
                                                    </Avatar>
                                                </TableCell>
                                                <TableCell className="font-medium">{user.name}</TableCell>
                                                <TableCell>{user.email}</TableCell>
                                                <TableCell>
                                                    <Badge
                                                        variant={user.role === "ADMIN" ? "destructive" : "secondary"}>
                                                        {user.role}
                                                    </Badge>
                                                </TableCell>
                                                <TableCell className="text-right">
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        onClick={() => handleToggleRole(user)}
                                                        title="Changer le rôle"
                                                    >
                                                        {user.role === "ADMIN" ?
                                                            <ShieldAlert className="w-4 h-4 text-orange-500"/> :
                                                            <ShieldCheck className="w-4 h-4 text-green-600"/>
                                                        }
                                                    </Button>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            )}
                        </CardContent>
                    </Card>
                </main>
            </div>
        </div>
    );
}