import React from "react";
import { Table, TableBody, TableCell, TableHead, TableRow, Chip } from "@mui/material";

export default function DeploymentTable({ pipelines }) {
    const statusColor = (status) => {
        switch(status) {
            case "SUCCESS": return "success";
            case "RUNNING": return "warning";
            case "FAILED": return "error";
            case "PENDING": return "default";
            default: return "default";
        }
    };

    return (
        <Table>
            <TableHead>
                <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Nom</TableCell>
                    <TableCell>Trigger</TableCell>
                    <TableCell>Status</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {pipelines.map(p => (
                    <TableRow key={p.id}>
                        <TableCell>{p.id}</TableCell>
                        <TableCell>{p.name}</TableCell>
                        <TableCell>{p.trigger}</TableCell>
                        <TableCell>
                            <Chip label={p.status} color={statusColor(p.status)} />
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
