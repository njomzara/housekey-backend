UPDATE agents SET
    full_name = CASE id
        WHEN 1 THEN 'Milica Jovanovic'
        WHEN 2 THEN 'Andrej Petrovic'
        WHEN 3 THEN 'Tereza Stojanovic'
        WHEN 4 THEN 'Mihailo Blagojevic'
        WHEN 5 THEN 'Mina Orlovic'
        ELSE full_name
    END,
    organization = 'HouseKey Novi Sad',
    email = CASE id
        WHEN 1 THEN 'milica.j@housekey.rs'
        WHEN 2 THEN 'andrej.p@housekey.rs'
        WHEN 3 THEN 'tereza.s@housekey.rs'
        WHEN 4 THEN 'mihailo.b@housekey.rs'
        WHEN 5 THEN 'mina.o@housekey.rs'
        ELSE email
    END,
    phone = CASE id
        WHEN 1 THEN '+381 21 267 1346'
        WHEN 2 THEN '+381 21 457 2308'
        WHEN 3 THEN '+381 21 617 2614'
        WHEN 4 THEN '+381 21 388 1637'
        WHEN 5 THEN '+381 21 388 1640'
        ELSE phone
    END
WHERE id BETWEEN 1 AND 5;

UPDATE property_listing SET title = 'Moderan stan na Grbavici', description = 'Svetao stan na Grbavici sa dve spavace sobe, terasom i funkcionalnim dnevnim boravkom.', city = 'Novi Sad', zip_code = '21000', latitude = 45.255133, longitude = 19.845176, formatted_address = 'Bulevar oslobodjenja 101, Novi Sad, Srbija', area_value = 72, area_unit = 'm2' WHERE id = 1;
UPDATE property_listing SET title = 'Kancelarija u Starom gradu', description = 'Poslovni prostor u Starom gradu, pogodan za agenciju ili IT tim, sa salom za sastanke i recepcijom.', city = 'Novi Sad', zip_code = '21000', latitude = 45.255802, longitude = 19.847726, formatted_address = 'Zmaj Jovina 12, Novi Sad, Srbija', area_value = 140, area_unit = 'm2' WHERE id = 2;
UPDATE property_listing SET title = 'Porodicna kuca u Sremskim Karlovcima', description = 'Porodicna kuca u Sremskim Karlovcima sa velikim dvoristem i pogledom na Frusku goru.', city = 'Sremski Karlovci', zip_code = '21205', latitude = 45.202850, longitude = 19.934900, formatted_address = 'Karlovackih djaka 7, Sremski Karlovci, Srbija', area_value = 195, area_unit = 'm2' WHERE id = 3;
UPDATE property_listing SET title = 'Prostran stan na Limanu', description = 'Prostran stan na Limanu IV blizu keja, parka i fakulteta.', city = 'Novi Sad', zip_code = '21000', latitude = 45.242500, longitude = 19.834700, formatted_address = 'Narodnog fronta 42, Novi Sad, Srbija', area_value = 90, area_unit = 'm2' WHERE id = 4;
UPDATE property_listing SET title = 'Kuca sa bazenom u Futogu', description = 'Kuca u Futogu sa bazenom, pomocnim objektom i uredjenom bastom.', city = 'Futog', zip_code = '21410', latitude = 45.237400, longitude = 19.704500, formatted_address = 'Cara Lazara 55, Futog, Srbija', area_value = 204, area_unit = 'm2' WHERE id = 5;
UPDATE property_listing SET title = 'Svetla kuca u Temerinu', description = 'Svetla porodicna kuca u Temerinu sa garazom i mirnim dvoristem.', city = 'Temerin', zip_code = '21235', latitude = 45.408900, longitude = 19.889200, formatted_address = 'Novosadska 18, Temerin, Srbija', area_value = 149, area_unit = 'm2' WHERE id = 6;
UPDATE property_listing SET title = 'Kancelarijski prostor na Detelinari', description = 'Moderan kancelarijski prostor na Detelinari sa odvojenim kancelarijama i parkingom.', city = 'Novi Sad', zip_code = '21000', latitude = 45.264800, longitude = 19.820300, formatted_address = 'Bulevar Evrope 35, Novi Sad, Srbija', area_value = 297, area_unit = 'm2' WHERE id = 7;
UPDATE property_listing SET title = 'Komforan stan na Telepu', description = 'Komforan stan na Telepu sa otvorenim dnevnim prostorom i dobrom povezanoscu.', city = 'Novi Sad', zip_code = '21000', latitude = 45.237700, longitude = 19.807400, formatted_address = 'Futoski put 121, Novi Sad, Srbija', area_value = 88, area_unit = 'm2' WHERE id = 8;
UPDATE property_listing SET title = 'Stan u Podbari', description = 'Stan u Podbari sa visokim plafonima, velikim prozorima i blizinom centra.', city = 'Novi Sad', zip_code = '21000', latitude = 45.262000, longitude = 19.851200, formatted_address = 'Kraljevica Marka 24, Novi Sad, Srbija', area_value = 160, area_unit = 'm2' WHERE id = 9;
UPDATE property_listing SET title = 'Kuca u Veterniku', description = 'Kuca u Veterniku sa terasom, uredjenim dvoristem i brzim izlazom ka Novom Sadu.', city = 'Veternik', zip_code = '21203', latitude = 45.255500, longitude = 19.758400, formatted_address = 'Kralja Petra I 33, Veternik, Srbija', area_value = 132, area_unit = 'm2' WHERE id = 10;
UPDATE property_listing SET title = 'Kreativna kancelarija u Kacu', description = 'Kreativna kancelarija u Kacu sa otvorenim timskim prostorom i salom za sastanke.', city = 'Kac', zip_code = '21241', latitude = 45.304100, longitude = 19.941500, formatted_address = 'Svetosavska 16, Kac, Srbija', area_value = 380, area_unit = 'm2' WHERE id = 11;
UPDATE property_listing SET title = 'Kuca sa krovnom terasom u Rumenki', description = 'Trospratna kuca u Rumenki sa krovnom terasom i garazom.', city = 'Rumenka', zip_code = '21201', latitude = 45.294700, longitude = 19.741400, formatted_address = 'Partizanska 9, Rumenka, Srbija', area_value = 184, area_unit = 'm2' WHERE id = 12;
UPDATE property_listing SET title = 'Dvoetanzi stan u Kovilju', description = 'Dvoetanzi stan u Kovilju sa zelenim dvoristem i prakticnim rasporedom.', city = 'Kovilj', zip_code = '21243', latitude = 45.235500, longitude = 20.023300, formatted_address = 'Laze Kostica 8, Kovilj, Srbija', area_value = 140, area_unit = 'm2' WHERE id = 13;
UPDATE property_listing SET title = 'Stan u mirnom delu Begeca', description = 'Stan u Begecu sa balkonom, obezbedjenim parkingom i mirnim okruzenjem.', city = 'Begec', zip_code = '21411', latitude = 45.238400, longitude = 19.608900, formatted_address = 'Kralja Petra I 14, Begec, Srbija', area_value = 156, area_unit = 'm2' WHERE id = 14;
UPDATE property_listing SET title = 'Kuca u Backom Petrovcu', description = 'Klasicna kuca u Backom Petrovcu sa ocuvanom stolarijom i pomocnim prostorijama.', city = 'Backi Petrovac', zip_code = '21470', latitude = 45.360600, longitude = 19.591700, formatted_address = 'Marsala Tita 22, Backi Petrovac, Srbija', area_value = 228, area_unit = 'm2' WHERE id = 15;
UPDATE property_listing SET title = 'Studio kancelarija u Zablju', description = 'Studio kancelarija u Zablju sa fleksibilnim radnim zonama i cajnom kuhinjom.', city = 'Zabalj', zip_code = '21230', latitude = 45.372200, longitude = 20.063900, formatted_address = 'Nikole Tesle 5, Zabalj, Srbija', area_value = 265, area_unit = 'm2' WHERE id = 16;
UPDATE property_listing SET title = 'Penthaus na Rotkvariji', description = 'Penthaus na Rotkvariji sa velikom terasom, pogledom na grad i privatnim ulazom.', city = 'Novi Sad', zip_code = '21000', latitude = 45.259100, longitude = 19.836700, formatted_address = 'Kisacka 31, Novi Sad, Srbija', area_value = 210, area_unit = 'm2' WHERE id = 17;
UPDATE property_listing SET title = 'Renovirana kuca u Titelu', description = 'Renovirana kuca u Titelu sa tremom, zrelim zelenilom i dodatnim studiom.', city = 'Titel', zip_code = '21240', latitude = 45.206700, longitude = 20.294400, formatted_address = 'Glavna 10, Titel, Srbija', area_value = 203, area_unit = 'm2' WHERE id = 18;
UPDATE property_listing SET title = 'Ugaona kancelarija u Srbobranu', description = 'Ugaona kancelarija u Srbobranu sa recepcijom, staklenim salama i dobrim pristupom.', city = 'Srbobran', zip_code = '21480', latitude = 45.553900, longitude = 19.802800, formatted_address = 'Svetog Save 6, Srbobran, Srbija', area_value = 480, area_unit = 'm2' WHERE id = 19;
UPDATE property_listing SET title = 'Moderna kuca u Beocinu', description = 'Moderna kuca u Beocinu sa otvorenim prizemljem, kuhinjskim ostrvom i ogradjenim dvoristem.', city = 'Beocin', zip_code = '21300', latitude = 45.191500, longitude = 19.720900, formatted_address = 'Karadjordjeva 19, Beocin, Srbija', area_value = 255, area_unit = 'm2' WHERE id = 20;

DELETE FROM listing_neighborhood WHERE property_listing_id BETWEEN 1 AND 20;
INSERT INTO listing_neighborhood (property_listing_id, sort_order, name) VALUES
    (1, 0, 'Grbavica'),
    (2, 0, 'Stari Grad'),
    (3, 0, 'Sremski Karlovci'),
    (4, 0, 'Liman IV'),
    (5, 0, 'Futog'),
    (6, 0, 'Temerin'),
    (7, 0, 'Detelinara'),
    (8, 0, 'Telep'),
    (9, 0, 'Podbara'),
    (10, 0, 'Veternik'),
    (11, 0, 'Kac'),
    (12, 0, 'Rumenka'),
    (13, 0, 'Kovilj'),
    (14, 0, 'Begec'),
    (15, 0, 'Backi Petrovac'),
    (16, 0, 'Zabalj'),
    (17, 0, 'Rotkvarija'),
    (18, 0, 'Titel'),
    (19, 0, 'Srbobran'),
    (20, 0, 'Beocin');

DELETE FROM listing_street WHERE property_listing_id BETWEEN 1 AND 20;
INSERT INTO listing_street (property_listing_id, sort_order, name) VALUES
    (1, 0, 'Bulevar oslobodjenja'),
    (2, 0, 'Zmaj Jovina'),
    (3, 0, 'Karlovackih djaka'),
    (4, 0, 'Narodnog fronta'),
    (5, 0, 'Cara Lazara'),
    (6, 0, 'Novosadska'),
    (7, 0, 'Bulevar Evrope'),
    (8, 0, 'Futoski put'),
    (9, 0, 'Kraljevica Marka'),
    (10, 0, 'Kralja Petra I'),
    (11, 0, 'Svetosavska'),
    (12, 0, 'Partizanska'),
    (13, 0, 'Laze Kostica'),
    (14, 0, 'Kralja Petra I'),
    (15, 0, 'Marsala Tita'),
    (16, 0, 'Nikole Tesle'),
    (17, 0, 'Kisacka'),
    (18, 0, 'Glavna'),
    (19, 0, 'Svetog Save'),
    (20, 0, 'Karadjordjeva');

UPDATE listing_additional_feature
SET name = CASE name
        WHEN 'Heat' THEN 'Grejanje'
        WHEN 'Roof' THEN 'Krov'
        WHEN 'Floors' THEN 'Podovi'
        WHEN 'Water' THEN 'Voda'
        WHEN 'Cross Streets' THEN 'Ukrstene ulice'
        WHEN 'Windows' THEN 'Prozori'
        WHEN 'Flat' THEN 'Sprat'
        WHEN 'Childroom' THEN 'Decija soba'
        WHEN 'Exposure' THEN 'Orijentacija'
        WHEN 'Building' THEN 'Zgrada'
        WHEN 'Outdoor' THEN 'Spoljasnji prostor'
        WHEN 'Parking' THEN 'Parking'
        WHEN 'Ceiling' THEN 'Plafon'
        WHEN 'Conference Rooms' THEN 'Sale za sastanke'
        WHEN 'Access' THEN 'Pristup'
        WHEN 'Storage' THEN 'Ostava'
        WHEN 'Workstations' THEN 'Radna mesta'
        ELSE name
    END,
    feature_value = CASE feature_value
        WHEN 'Natural Gas' THEN 'Prirodni gas'
        WHEN 'Composition/Shingle' THEN 'Kompozitni crep'
        WHEN 'Wall-to-Wall Carpet' THEN 'Tepih od zida do zida'
        WHEN 'District/Public' THEN 'Gradski vodovod'
        WHEN 'Orangethorpe-Gilbert' THEN 'Bulevar oslobodjenja - Jevrejska'
        WHEN 'Skylights' THEN 'Krovni prozori'
        WHEN 'Central' THEN 'Centralno'
        WHEN 'Wide-Plank Hardwood' THEN 'Siroki parket'
        WHEN 'West' THEN 'Zapad'
        WHEN 'Doorman' THEN 'Portir'
        WHEN 'Forced Air' THEN 'Duvanje toplog vazduha'
        WHEN 'Private Deck' THEN 'Privatna terasa'
        WHEN 'Driveway' THEN 'Prilaz'
        WHEN 'Exposed Timber' THEN 'Vidljive grede'
        WHEN 'Elevator' THEN 'Lift'
        WHEN 'Heat Pump' THEN 'Toplotna pumpa'
        WHEN 'Membrane' THEN 'Membranski'
        WHEN 'Rooftop Deck' THEN 'Krovna terasa'
        WHEN 'Attached Garage' THEN 'Garaza uz objekat'
        WHEN 'Radiant' THEN 'Podno grejanje'
        WHEN 'Hardwood' THEN 'Parket'
        WHEN 'Garden' THEN 'Basta'
        WHEN 'Private Basement' THEN 'Privatni podrum'
        WHEN 'Engineered Hardwood' THEN 'Troslojni parket'
        WHEN 'Balcony' THEN 'Balkon'
        WHEN 'Secure Garage' THEN 'Obezbedjena garaza'
        WHEN 'Slate' THEN 'Skriljac'
        WHEN 'Patio' THEN 'Dvoriste'
        WHEN 'High Open Ceiling' THEN 'Visok otvoren plafon'
        WHEN 'Freight Elevator' THEN 'Teretni lift'
        WHEN 'White Oak' THEN 'Beli hrast'
        WHEN 'Wraparound Terrace' THEN 'Terasa oko stana'
        WHEN 'Private Elevator' THEN 'Privatni lift'
        WHEN 'Oak Hardwood' THEN 'Hrastov parket'
        WHEN 'Detached Studio' THEN 'Odvojeni studio'
        WHEN 'Corner' THEN 'Ugao'
        WHEN 'Metal' THEN 'Metalni'
        WHEN 'Fenced Yard' THEN 'Ogradjeno dvoriste'
        ELSE feature_value
    END
WHERE property_listing_id BETWEEN 1 AND 20;

DELETE FROM listing_floor_plan WHERE property_listing_id BETWEEN 1 AND 20;
INSERT INTO listing_floor_plan (property_listing_id, sort_order, name, description, area_value, area_unit, rooms, baths, image) VALUES
    (1, 0, 'Osnova stana', 'Dnevna zona povezana je sa kuhinjom, dok su spavace sobe odvojene od ulaza.', 72, 'm2', 3, 2, 'images/others/plan-1.jpg'),
    (2, 0, 'Osnova kancelarije', 'Recepcija, otvoreni radni prostor i sala za sastanke nalaze se na jednom nivou.', 140, 'm2', 5, 2, 'images/others/plan-1.jpg'),
    (3, 0, 'Osnova kuce', 'Porodicni raspored sa dnevnom zonom u prizemlju i sobama okrenutim ka dvoristu.', 195, 'm2', 6, 1, 'images/others/plan-2.jpg'),
    (4, 0, 'Osnova stana', 'Prostran dnevni boravak izlazi na terasu, a sobe su smestene prema mirnijoj strani zgrade.', 90, 'm2', 4, 2, 'images/others/plan-2.jpg'),
    (5, 0, 'Osnova kuce', 'Prizemlje je povezano sa bastom i bazenom, uz odvojen pomocni prostor.', 204, 'm2', 5, 1, 'images/others/plan-1.jpg'),
    (6, 0, 'Osnova kuce', 'Kuca ima centralni dnevni prostor, dve spavace sobe i izlaz ka dvoristu.', 149, 'm2', 5, 2, 'images/others/plan-2.jpg'),
    (7, 0, 'Osnova kancelarije', 'Kancelarije su organizovane oko centralnog hodnika sa zasebnom salom za sastanke.', 297, 'm2', 7, 2, 'images/others/plan-1.jpg'),
    (8, 0, 'Osnova stana', 'Otvoren dnevni prostor povezuje kuhinju, trpezariju i terasu.', 88, 'm2', 4, 2, 'images/others/plan-2.jpg'),
    (9, 0, 'Osnova stana', 'Stan ima veliki dnevni boravak, dve spavace sobe i radni kutak uz prozor.', 160, 'm2', 4, 2, 'images/others/plan-1.jpg'),
    (10, 0, 'Osnova kuce', 'Kuca je jednospratna, sa dnevnim delom otvorenim prema privatnoj terasi.', 132, 'm2', 5, 2, 'images/others/plan-2.jpg'),
    (11, 0, 'Osnova kancelarije', 'Otvoren timski prostor dopunjen je manjim kancelarijama i kuhinjom.', 380, 'm2', 7, 3, 'images/others/plan-1.jpg'),
    (12, 0, 'Osnova kuce', 'Tri nivoa povezuju garazu, dnevni sprat, spavaci deo i pristup krovnoj terasi.', 184, 'm2', 6, 3, 'images/others/plan-2.jpg'),
    (13, 0, 'Osnova stana', 'Dvoetanzi raspored odvaja prostor za prijem gostiju od mirnog spavaceg dela.', 140, 'm2', 5, 2, 'images/others/plan-1.jpg'),
    (14, 0, 'Osnova stana', 'Efikasan raspored sa dve spavace sobe, otvorenim boravkom i balkonom.', 156, 'm2', 4, 2, 'images/others/plan-2.jpg'),
    (15, 0, 'Osnova kuce', 'Tradicionalan raspored sa formalnim sobama, spavacim delom i pomocnim prostorijama.', 228, 'm2', 7, 3, 'images/others/plan-1.jpg'),
    (16, 0, 'Osnova kancelarije', 'Studio prostor ima zonu za sastanke, cajnu kuhinju i fleksibilna radna mesta.', 265, 'm2', 5, 2, 'images/others/plan-2.jpg'),
    (17, 0, 'Osnova penthausa', 'Privatan ulaz vodi u veliki dnevni prostor povezan sa terasom oko stana.', 210, 'm2', 5, 3, 'images/others/plan-1.jpg'),
    (18, 0, 'Osnova kuce', 'Trem uvodi u centralni dnevni prostor, a spavace sobe su rasporedjene uz hodnik.', 203, 'm2', 7, 2, 'images/others/plan-2.jpg'),
    (19, 0, 'Osnova kancelarije', 'Ugaoni plan sadrzi recepciju, sale za sastanke, privatne kancelarije i timsku zonu.', 480, 'm2', 9, 3, 'images/others/plan-1.jpg'),
    (20, 0, 'Osnova kuce', 'Otvoreno prizemlje povezuje kuhinju, trpezariju i dnevni boravak, sa sobama na spratu.', 255, 'm2', 8, 3, 'images/others/plan-2.jpg');

UPDATE listing_video
SET name = CASE sort_order
        WHEN 0 THEN 'Video obilazak'
        ELSE 'Video sa EmbedVideoService'
    END
WHERE property_listing_id BETWEEN 1 AND 20;
