export const pipelines = [
    {
        id: 1,
        name: "Pipeline #1",
        status: "SUCCESS",
        trigger: "MANUAL",
        steps: [
            { name: "Git Clone", status: "SUCCESS" },
            { name: "Build & Test", status: "SUCCESS" },
            { name: "Docker Build", status: "SUCCESS" },
            { name: "Deploy", status: "SUCCESS" }
        ]
    },
    {
        id: 2,
        name: "Pipeline #2",
        status: "RUNNING",
        trigger: "GITHUB",
        steps: [
            { name: "Git Clone", status: "SUCCESS" },
            { name: "Build & Test", status: "RUNNING" },
            { name: "Docker Build", status: "PENDING" },
            { name: "Deploy", status: "PENDING" }
        ]
    }
];

export const users = [
    { id: 1, name: "Alice", role: "ADMIN" },
    { id: 2, name: "Bob", role: "DEVELOPER" }
];
