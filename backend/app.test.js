const request = require('supertest');
const https = require('https');
const fs = require('fs');
const { MongoClient } = require('mongodb');

jest.mock('https');
https.createServer.mockReturnValue({ 
    listen: jest.fn((port, callback) => {
        callback();
    }) 
});

originalReadFileSync = fs.readFileSync;
fs.readFileSync = jest.fn((filePath, encoding) => {
    if (filePath === '/etc/letsencrypt/live/milestone1.canadacentral.cloudapp.azure.com/privkey.pem'
        || filePath === '/etc/letsencrypt/live/milestone1.canadacentral.cloudapp.azure.com/fullchain.pem') {
        return '';
    }
    
    return originalReadFileSync(filePath, encoding);
});

jest.mock('mongodb');
var mockCollection = {
    findOne: jest.fn(),
    find: jest.fn(),
    insertOne: jest.fn(),
    updateOne: jest.fn()
};
MongoClient.mockReturnValue({
    connect: jest.fn(),
    db: jest.fn().mockReturnValue({
        collection: jest.fn().mockReturnValue(mockCollection)
    })
});

const admin = require('firebase-admin');
jest.mock('firebase-admin');
admin.messaging = jest.fn().mockReturnValue('');
admin.credential.cert.mockReturnValue('');

const app = require('./app.js');
const { randomInt } = require('crypto');

// Interface GET /leaderboard
describe("Retrieve the leaderboard", () => {
    /**
     * Input: N/A
     * Expected status code: 200
     * Expected behaviour: The stats of the 10 players with the highest elo are retrieved
     * Expected output: Array of up to 10 player stats, including: id, name, elo, games won, games lost, average game duration, and average game path length
     */
    test("Successfully retrieve leaderboard with players", async () => {
        const player = mockPlayer();

        var toArray = jest.fn().mockReturnValueOnce([ player ]);
        var limit = jest.fn().mockReturnValueOnce({ toArray });
        var sort = jest.fn().mockReturnValueOnce({ limit });
        mockCollection.find.mockReturnValueOnce({ sort });

        const response = await request(app).get('/leaderboard');

        expect(response.status).toBe(200);
        expect(response.body).toBeInstanceOf(Array);
        expect(response.body.length).toBe(1);
        expect(JSON.stringify(response.body[0])).toBe(JSON.stringify(player));
        expect(toArray).toHaveBeenCalled();
        expect(limit).toHaveBeenCalledWith(10);
        expect(sort).toHaveBeenCalled();
    });

    /**
     * Input: N/A
     * Expected status code: 200
     * Expected behaviour: The stats of the 10 players with the highest elo are retrieved
     * Expected output: Empty array when there are no players
     */
    test("Retrieve leaderboard with no players", async () => {
        var toArray = jest.fn().mockReturnValueOnce([]);
        var limit = jest.fn().mockReturnValueOnce({ toArray });
        var sort = jest.fn().mockReturnValueOnce({ limit });
        mockCollection.find.mockReturnValueOnce({ sort });

        // Perform actions to set up the scenario with no players (e.g., clear database)
        const response = await request(app).get('/leaderboard');

        expect(response.status).toBe(200);
        expect(response.body).toBeInstanceOf(Array);
        expect(response.body.length).toBe(0);
        expect(toArray).toHaveBeenCalled();
        expect(limit).toHaveBeenCalledWith(10);
        expect(sort).toHaveBeenCalled();
    });

    /**
     * Input: N/A
     * Expected status code: 500
     * Expected behaviour: The server should error out
     * Expected output: An internal server error response
     */
    test("Database retrieval error", async () => {
        var toArray = jest.fn().mockRejectedValue(new Error('Test Error'));

        // Perform actions to set up the scenario with no players (e.g., clear database)
        const response = await request(app).get('/leaderboard');

        expect(response.status).toBe(500);
    });
});


// Interface GET /player/:id
describe("Retrieve a player's information", () => {
    /**
     * Input: A player's id
     * Expected status code: 200
     * Expected behaviour: The player's information should be retrieved
     * Expected output: The player's stats, including their id, name, elo, games won, games lost, average game duration, and average game path length
     */
    test("Retrieve a single player's information", async () => {
        const player = mockPlayer();

        mockCollection.findOne.mockReturnValue(player);

        const response = await request(app).get('/player/' + player._id);
        expect(response.status).toBe(200);
        expect(JSON.stringify(response.body)).toBe(JSON.stringify(player));
        expect(mockCollection.findOne).toHaveBeenCalledWith({ _id: player._id })
    });

    /**
     * Input: A nonexistent player id (300)
     * Expected status code: 404
     * Expected behaviour: The server should simply return a 404 error
     * Expected output: A 404 response
     */
    test("Retrieve a nonexistent player's information", async () => {
        mockCollection.findOne.mockReturnValue(null);

        const response = await request(app).get('/player/300');
        expect(response.status).toBe(404);
        expect(mockCollection.findOne).toHaveBeenCalledWith({ _id: 300 })
    });

    /**
     * Input: N/A
     * Expected status code: 500
     * Expected behaviour: The server should error out
     * Expected output: An internal server error response
     */
    test("Database retrieval error", async () => {
        mockCollection.findOne.mockRejectedValue(new Error("Test error"));

        const response = await request(app).get('/player/20');
        expect(response.status).toBe(500);
        expect(mockCollection.findOne).toHaveBeenCalledWith({ _id: 20 })
    });
});

// Interface GET /signIn/:id
describe("Testing player signIn", () => {
    /**
     * Input: A player's id
     * Expected status code: 200
     * Expected behaviour: The player should be "signed in" and can now enter games.
     * Expected output: Should return the players ID as signin confirmation
     */
    test("SignIn Player", async () => {
        const player = mockPlayer();

        mockCollection.findOne.mockReturnValue(player);

        const response = await request(app).get('/signIn/' + player._id);
        expect(response.status).toBe(200);
        expect(JSON.stringify(response.body)).toBe(JSON.stringify(player._id));
        expect(mockCollection.findOne).toHaveBeenCalledWith({ _id: player._id })
    });

    /**
     * Input: A nonexistent player id (300)
     * Expected status code: 404
     * Expected behaviour: The server should simply return a 404 error
     * Expected output: A 404 response
     */
    test("Retrieve a nonexistent player's information", async () => {
        mockCollection.findOne.mockReturnValue(null);

        const response = await request(app).get('/signIn/300');
        expect(response.status).toBe(404);
        expect(mockCollection.findOne).toHaveBeenCalledWith({ _id: 300 })
    });

    /**
     * Input: N/A
     * Expected status code: 500
     * Expected behaviour: The server should error out
     * Expected output: An internal server error response
     */
    test("Database retrieval error", async () => {
        mockCollection.findOne.mockRejectedValue(new Error("Test error"));

        const response = await request(app).get('/player/20');
        expect(response.status).toBe(500);
        expect(mockCollection.findOne).toHaveBeenCalledWith({ _id: 20 })
    });
});


function mockPlayer(
    name = "Julia Rubin", 
    _id = randomInt(1000000), 
    elo = randomInt(1000000), 
    gamesWon = randomInt(1000000), 
    gamesLost = randomInt(1000000), 
    avgGameDuration = randomInt(1000000), 
    avgGamePathLength = randomInt(1000000)
) {
    return {
        _id,
        name,
        elo,
        gamesWon,
        gamesLost,
        avgGameDuration,
        avgGamePathLength
    };
}