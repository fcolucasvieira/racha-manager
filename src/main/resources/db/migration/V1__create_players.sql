CREATE TABLE players
(
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_players PRIMARY KEY (id)
);