package model;


public record UserData(String username, String password, String email) {
}

//A user
//used by UserService for registration and login auth
//Stored and accessed by DataAccess methods
