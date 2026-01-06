// src/components/JobBox.jsx
import { CheckCircle2, XCircle, Loader2, Clock } from "lucide-react";

export default function JobBox({ job }) {
    const icons = {
        SUCCESS: <CheckCircle2 className="w-5 h-5 text-green-500" />,
        FAILED: <XCircle className="w-5 h-5 text-red-500" />,
        RUNNING: <Loader2 className="w-5 h-5 text-blue-500 animate-spin" />,
        PENDING: <Clock className="w-5 h-5 text-muted-foreground" />,
    };

    return (
        <div className="rounded-xl border bg-card text-card-foreground shadow hover:shadow-md transition-shadow p-4 flex items-center justify-between min-w-[200px]">
            <div className="flex items-center gap-3">
                {icons[job.status]}
                <div>
                    <p className="text-sm font-semibold">{job.name}</p>
                    <p className="text-xs text-muted-foreground">{job.duration || "---"}</p>
                </div>
            </div>
        </div>
    );
}