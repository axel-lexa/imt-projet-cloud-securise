import {useEffect, useState} from "react";
import {Navigate, Outlet} from "react-router-dom";
import {checkAuthStatus} from "../api/cicdApi";
import {Loader2} from "lucide-react";

const ProtectedRoute = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(null);

    useEffect(() => {
        const verifyAuth = async () => {
            const isAuth = await checkAuthStatus();
            setIsAuthenticated(isAuth);
        };
        verifyAuth();
    }, []);

    if (isAuthenticated === null) {
        // Affichage pendant la vérification
        return <div className="flex h-screen items-center justify-center"><Loader2 className="animate-spin"/></div>;
    }

    return isAuthenticated ? <Outlet/> : <Navigate to="/login" replace/>;
};

export default ProtectedRoute;