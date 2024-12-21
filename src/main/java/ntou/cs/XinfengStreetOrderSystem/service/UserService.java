package ntou.cs.XinfengStreetOrderSystem.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ntou.cs.XinfengStreetOrderSystem.entity.User;
import ntou.cs.XinfengStreetOrderSystem.entity.User.OrderHistory;
import ntou.cs.XinfengStreetOrderSystem.repository.MenuItemRepository;
import ntou.cs.XinfengStreetOrderSystem.repository.UserRepository;

@Service
public class UserService {

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username) != null; // 判斷用戶名是否已存在
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Optional<User> findUserById(String id) {
        System.out.println("Searching for user with ID: " + id);
        Optional<User> user = userRepository.findById(id);
        System.out.println("User found: " + user.isPresent());
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username); // 調用 Repository 方法
    }

    // Registration logic
    public boolean register(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return false;
        }
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        System.out.println("User registered: " + user.getUsername());
        return true;
    }

    // Login logic
    public boolean login(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            System.out.println("User logged in: " + existingUser.getUsername());
            return true;
        }
        System.out.println("Login failed for username: " + user.getUsername());
        return false;
    }

    // Get all favorite items for a user
    public List<User.FavoriteItem> getFavoriteItems(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getFavoriteItems();
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Add a favorite item for a user
    public void addFavoriteItem(String userId, User.FavoriteItem favoriteItem) {
        // Check if the menu item exists in the menu repository
        if (!menuItemRepository.existsById(favoriteItem.getMenuItemId())) {
            throw new IllegalArgumentException("Menu item not found: " + favoriteItem.getMenuItemId());
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the favorite item already exists
            if (user.getFavoriteItems().stream()
                    .anyMatch(item -> item.getMenuItemId().equals(favoriteItem.getMenuItemId()))) {
                throw new IllegalArgumentException("Favorite item already exists: " + favoriteItem.getMenuItemId());
            }

            // Add the favorite item
            favoriteItem.setAddedAt(new Date()); // Set the current timestamp
            user.getFavoriteItems().add(favoriteItem);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    // Remove a favorite item for a user
    public void removeFavoriteItem(String userId, String menuItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getFavoriteItems().removeIf(item -> item.getMenuItemId().equals(menuItemId));
        userRepository.save(user);
    }
    // 更新訂單歷史
    public void updateOrderHistory(String userId, String orderId) {
        // 查找用戶
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 創建新的訂單歷史
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setOrderId(orderId);  // 設定訂單 ID
        orderHistory.setOrderedAt(new Date());  // 設定訂單時間為當前時間

        // 加入新的訂單歷史到用戶的訂單歷史
        user.getOrderHistory().add(orderHistory);

        // 更新用戶資料
        userRepository.save(user);
        System.out.println("UpdateHistorySuccess:"+ userId);
    }
}
