package com.example.taxi;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository để thao tác với collection "users" trên Firestore.
 * Thay thế hoàn toàn AppUserRepository (JPA).
 */
@Repository
public class FirestoreUserRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "users";

    public FirestoreUserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /** Lưu hoặc cập nhật user (document ID = username) */
    public AppUser save(AppUser user) {
        try {
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(user.getUsername());
            }
            firestore.collection(COLLECTION).document(user.getId()).set(user).get();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu user lên Firestore: " + e.getMessage(), e);
        }
        return user;
    }

    /** Tìm user theo username */
    public Optional<AppUser> findByUsername(String username) {
        try {
            var docs = firestore.collection(COLLECTION)
                    .whereEqualTo("username", username)
                    .get().get().getDocuments();
            if (docs.isEmpty()) return Optional.empty();
            return Optional.ofNullable(docs.get(0).toObject(AppUser.class));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm user theo username: " + e.getMessage(), e);
        }
    }

    /** Tìm user theo email */
    public Optional<AppUser> findByEmail(String email) {
        try {
            var docs = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email)
                    .get().get().getDocuments();
            if (docs.isEmpty()) return Optional.empty();
            return Optional.ofNullable(docs.get(0).toObject(AppUser.class));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm user theo email: " + e.getMessage(), e);
        }
    }

    /** Tìm tất cả user theo role */
    public List<AppUser> findByRole(String role) {
        try {
            List<AppUser> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : firestore.collection(COLLECTION)
                    .whereEqualTo("role", role)
                    .get().get().getDocuments()) {
                AppUser u = doc.toObject(AppUser.class);
                if (u != null) result.add(u);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm user theo role: " + e.getMessage(), e);
        }
    }

    /** Lấy tất cả users */
    public List<AppUser> findAll() {
        try {
            List<AppUser> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : firestore.collection(COLLECTION).get().get().getDocuments()) {
                AppUser u = doc.toObject(AppUser.class);
                if (u != null) result.add(u);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách users: " + e.getMessage(), e);
        }
    }

    /** Kiểm tra username tồn tại không */
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    /** Xóa user theo username */
    public void deleteByUsername(String username) {
        try {
            var docs = firestore.collection(COLLECTION)
                    .whereEqualTo("username", username)
                    .get().get().getDocuments();
            for (var doc : docs) {
                doc.getReference().delete().get();
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa user: " + e.getMessage(), e);
        }
    }
}
