package com.alice.dbclasses;


import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SerializationUtils;

import java.util.*;
import java.util.function.BiConsumer;

@Component
public class UpdateDB {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Writes data to users and drives tables as a transaction
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserDrive(User user, Drive drive) {
        byte[] data = SerializationUtils.serialize(user);

        jdbcTemplate.update("insert into users (id, blob) values (?, ?) on conflict(id) do update set blob = excluded.blob",
                user.getUserID(), data);

        data = SerializationUtils.serialize(drive);

        jdbcTemplate.update("insert into drives (id, blob) values (?, ?) on conflict(id) do update set blob = excluded.blob",
                drive.getDriveID(), data);

        jdbcTemplate.update("insert into update_drives (blob) values (?)", data);
    }


    /**
     * Writes data to users and logpass tables as a transaction
     * @param ID is unique because it was given by users_ids sequence in {@code LogPassService}
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserLogPass(User user, Long ID, String login, String password) {
        byte[] data = SerializationUtils.serialize(user);

        jdbcTemplate.update("insert into logpass (log, pass, id) values (?, ?, ?)",
                login, password, ID);

        jdbcTemplate.update("insert into users (id, blob) values (?, ?)",
                ID, data);
    }

    /**
     * Sends updated drives to Kafka and deletes sent drives
     * @param sender Sends drive and waits for callback
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendUpdateDrives(BiConsumer<Long, byte[]> sender){
        List<Map<String, Object>> idsDrivesFromTable = jdbcTemplate.queryForList("select id, blob from update_drives");

        Map<Long, byte[]> drivesToSend = new TreeMap<>();

        for (Map<String, Object> idDrive : idsDrivesFromTable) {
            Long id = (Long) idDrive.get("id");
            byte[] drive = (byte[]) idDrive.get("blob");

            drivesToSend.put(id, drive);
        }

        if (!drivesToSend.isEmpty()) {

            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            Map<String, Set> params = Collections.singletonMap("ids", drivesToSend.keySet());
            namedTemplate.update("delete from update_drives where id in (:ids)", params);

            drivesToSend.forEach(sender);
        }
    }

}
