// src/pages/Users.jsx
import React, { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../components/ui/table.jsx";
import { Badge } from "../components/ui/badge.jsx";
import { getUsers } from "../api/cicdApi";

export default function Users() {
    const [userList, setUserList] = useState([]);

    useEffect(() => {
        getUsers().then(setUserList);
    }, []);

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar activePage="Utilisateurs" />
            <div className="flex-1 flex flex-col">
                <Topbar title="Gestion des Utilisateurs" />
                <main className="p-6">
                    <div className="bg-white rounded-xl border shadow-sm overflow-hidden">
                        <Table>
                            <TableHeader>
                                <TableRow className="bg-muted/50">
                                    <TableHead>Nom</TableHead>
                                    <TableHead>Email</TableHead>
                                    <TableHead>Rôle</TableHead>
                                    <TableHead>Statut</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {userList.map((user) => (
                                    <TableRow key={user.id}>
                                        <TableCell className="font-medium">{user.name}</TableCell>
                                        <TableCell>{user.email}</TableCell>
                                        <TableCell>
                                            <Badge variant="secondary">{user.role}</Badge>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <div className={`w-2 h-2 rounded-full ${user.status === 'Active' ? 'bg-green-500' : 'bg-gray-300'}`} />
                                                {user.status}
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                </main>
            </div>
        </div>
    );
}