package com.alice.dbclasses;


import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.SerializationUtils;

@Component
public class UpdateDB {

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @Autowired
//    public UpdateDB(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }

    /**
     * Writes data to users table and drives table as a transaction
     */
    @Transactional
    public void updateUserDrive(User user, Drive drive) {
        byte[] data = SerializationUtils.serialize(user);

        jdbcTemplate.update("insert into users (id, blob) values (?, ?) on conflict(id) do update set blob = excluded.blob",
                user.getUserID(), data);

        data = SerializationUtils.serialize(drive);

        jdbcTemplate.update("insert into drives (id, blob) values (?, ?) on conflict(id) do update set blob = excluded.blob",
                drive.getDriveID(), data);
    }


    /**
     * Writes data to users table and logpass table as a transaction
     * @param ID is unique because it was given by users_ids sequence in {@code LogPassService}
     */
    @Transactional
    public void updateUserLogPass(User user, Long ID, String login, String password) throws Exception{
        byte[] data = SerializationUtils.serialize(user);

   //     System.err.println(TransactionSynchronizationManager.isActualTransactionActive());

        jdbcTemplate.update("insert into logpass (log, pass, id) values (?, ?, ?)",
                login, password, ID);

        jdbcTemplate.update("insert into users (id, blob) values (?, ?)",
                ID, data);
    }

}