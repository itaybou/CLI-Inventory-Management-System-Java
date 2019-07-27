package presistence.dao;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Interface implemented by DAO objects
 * @param <T> DTO type
 * @param <K1> Type of the DAO key
 */
public interface DAO<T, K1, K2, K3> {

    /**
     * Updates the user that is recognize by @param key with the details of the DTO object t
     * @param t the DTO object to update details from
     * @param key The key of the tuple to update details in.
     * @return SUCCESS, if operation succeeded and FAIL otherwise
     */
    Result update(T t, K1 key1, K2 key2, K3 key3);

    /**
     * Deletes the entity with the same key as @param t if the key of @param t exists in database
     * @param t the DTO object used to recognize the row to delete.
     * @return SUCCESS, if operation succeeded and FAIL otherwise
     */
    Result delete(T t);

    /**
     * Inserts a new tuple with the same parameters as @param t if the key of @param t does not already exists in database
     * @param t the DTO object used to take new tuple details from.
     * @return SUCCESS, if operation succeeded and FAIL otherwise
     */
    Result insert(T t);

    /**
     * Finds row with the same key as @param t and return it.
     * @param t the DTO object used to recognize the row to return.
     * @return A DTO object of type T with the details of the row selected if exists, null otherwise.
     */
    T findByKey(T t);


    /**
     * Sets the connection instance for current DAO object
     * @param conn the connection established with the database
     */
    void setConnection(Connection conn);

}
