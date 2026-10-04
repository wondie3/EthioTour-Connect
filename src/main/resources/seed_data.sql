-- EthioTour Seed Data
-- Destinations
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (1, 'Rock-Hewn Churches, Lalibela', '11 medieval monolithic cave churches from the 13th century.', 'Amhara', 2500, 'UNESCO World Heritage Site. Modest dress required.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (2, 'Simien National Park', 'Massive erosion over the years on the Ethiopian plateau has created one of the most spectacular landscapes in the world.', 'Amhara', 4533, 'UNESCO World Heritage Site. Park scout mandatory.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (3, 'Fasil Ghebbi, Gondar Region', 'The fortress-city of Fasil Ghebbi was the residence of the Ethiopian emperor Fasilides and his successors.', 'Amhara', 2133, 'UNESCO World Heritage Site. Open daily.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (4, 'Aksum', 'The ruins of the ancient city of Aksum mark the heart of ancient Ethiopia.', 'Tigray', 2131, 'UNESCO World Heritage Site. Museum pass required.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (5, 'Lower Valley of the Omo', 'A prehistoric site near Lake Turkana where many hominid fossils have been found.', 'South Ethiopia', 500, 'UNESCO World Heritage Site. Guide mandatory.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (6, 'Tiya', 'An archaeological site containing 36 monuments, including 32 carved stelae.', 'Central Ethiopia', 2300, 'UNESCO World Heritage Site. Quiet respect required.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (7, 'Harar Jugol, the Fortified Historic Town', 'The walled city of Harar is considered the fourth holy city of Islam.', 'Harari', 1885, 'UNESCO World Heritage Site. Local guide recommended.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (8, 'Konso Cultural Landscape', 'A 55 km2 arid property of stone-walled terraces and fortified settlements.', 'South Ethiopia', 1650, 'UNESCO World Heritage Site. Community-led tours.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (9, 'Lower Valley of the Awash', 'Site where the remains of Lucy (Australopithecus afarensis) were discovered.', 'Afar', 600, 'UNESCO World Heritage Site. Historic landmark.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (10, 'Bale Mountains National Park', 'A mosaic of high-altitude plateau, volcanic peaks, and ancient forests.', 'Oromia', 4377, 'UNESCO World Heritage Site. Diverse endemic wildlife.', 1);
INSERT OR IGNORE INTO Destinations (id, name, description, region, altitude, protocol, active) VALUES (11, 'Gedeo Cultural Landscape', 'A forest-based agroforestry system using the multilayer cultivation of enset.', 'South Ethiopia', 2000, 'UNESCO World Heritage Site. Unique agricultural heritage.', 1);

-- Tours
INSERT OR IGNORE INTO Tours (id, name, description, destinationId, startDate, endDate, maxParticipants, residentPrice, nonResidentPrice, status) VALUES (1, 'Spiritual Lalibela', 'A 3-day spiritual tour of the rock churches.', 1, '2026-06-15', '2026-06-18', 12, 3500.0, 15000.0, 'PLANNED');
INSERT OR IGNORE INTO Tours (id, name, description, destinationId, startDate, endDate, maxParticipants, residentPrice, nonResidentPrice, status) VALUES (2, 'Summit of Africa Trek', '5-day trek in the Simien Mountains.', 2, '2026-07-01', '2026-07-06', 8, 6000.0, 28000.0, 'PLANNED');
INSERT OR IGNORE INTO Tours (id, name, description, destinationId, startDate, endDate, maxParticipants, residentPrice, nonResidentPrice, status) VALUES (3, 'Gates of Harar', 'A cultural walk through the ancient city.', 7, '2026-06-20', '2026-06-22', 15, 2000.0, 8000.0, 'PLANNED');
INSERT OR IGNORE INTO Tours (id, name, description, destinationId, startDate, endDate, maxParticipants, residentPrice, nonResidentPrice, status) VALUES (4, 'The Afar Adventure', 'Explore the salt flats and Erta Ale volcano.', 9, '2026-08-10', '2026-08-14', 6, 18000.0, 75000.0, 'PLANNED');

-- Users
INSERT OR IGNORE INTO Users (id, username, email, phone, role) VALUES (1, 'admin', 'admin@ethiotour.com', '+251911000000', 'ADMIN');
INSERT OR IGNORE INTO Users (id, username, email, phone, role) VALUES (2, 'tsega_guide', 'tsega@ethiotour.com', '+251911223344', 'TOUR_GUIDE');

-- Bookings
INSERT OR IGNORE INTO Bookings (tourId, customerName, customerEmail, customerPhone, isResident, participantsCount, totalPrice, status) VALUES (1, 'John Doe', 'john@example.com', '+123456789', 0, 2, 30000.0, 'CONFIRMED');
INSERT OR IGNORE INTO Bookings (tourId, customerName, customerEmail, customerPhone, isResident, participantsCount, totalPrice, status) VALUES (2, 'Abebe Bikila', 'abebe@example.com', '+251911911911', 1, 1, 6000.0, 'CONFIRMED');
