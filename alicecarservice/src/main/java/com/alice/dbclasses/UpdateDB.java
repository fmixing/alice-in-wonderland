package com.alice.dbclasses;


import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.user.User;
import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SerializationUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
@ManagedResource
public class UpdateDB {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private volatile int limit = 1000;

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
     * Sends limited amount of updated drives to Kafka and deletes sent drives as a transaction
     * @param sender Sends drive and waits for callback
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendUpdateDrives(BiFunction<Long, byte[], Future<?>> sender){
        List<Map<Long, byte[]>> idsDrivesFromTable = jdbcTemplate.query(
                "select id, blob from update_drives order by id ASC limit ?",
                (resultSet, i) -> {
                    long id = resultSet.getLong(1);
                    byte[] blob = resultSet.getBytes(2);
                    return Collections.singletonMap(id, blob);
                }, limit);

        List<Long> ids = idsDrivesFromTable.stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toList());

        if (!ids.isEmpty()) {
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            Map<String, List<Long>> params = Collections.singletonMap("ids", ids);
            namedTemplate.update("delete from update_drives where id in (:ids)", params);

            List<? extends Future<?>> futures = idsDrivesFromTable.stream().flatMap(map -> map.entrySet().stream()
                    .map(entry -> sender.apply(entry.getKey(), entry.getValue())))
                    .collect(Collectors.toList());

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }

    @ManagedAttribute
    public int getLimit() {
        return limit;
    }

    @ManagedAttribute
    public void setLimit(int limit) {
        this.limit = limit;
    }
}
