CREATE TABLE  users ( 

    id UUID PRIMARY KEY ,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(80) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL

);

CREATE TABLE projects(
    id UUID PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
   FOREIGN KEY (owner_id) REFERENCES users(id)
);


CREATE TABLE tasks ( 
    id UUID PRIMARY KEY,
    title VARCHAR(30)NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    priority VARCHAR(40) NOT NULL,
    assignee_id UUID,
    project_id UUID NOT NULL,
    due_date DATE,
    created_at TIMESTAMP NOT NULL,


    FOREIGN KEY (assignee_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE project_members (
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,

    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);