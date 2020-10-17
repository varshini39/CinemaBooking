# CinemaBooking

This repository contains application related to cinema booking.

I have used 5 data models like Movies, Screen, Hall, Shows and Users (I have committed the sql model too). 

DBOperations.java is a singleton class used to perform all the DB operations.
RedisUtil.java is used for caching purpose during booking.
UserAuth.java checks the basic authentication for the API.

Bookings and Payments are implemented using synchronised thread functions and singleton class.

There are three APIs:
1. GET /cinema/booking/{user_id} - Fetch the booking of the User for particular show.
2. POST /cinema/booking - Add the booking for the given user following the mentioned usecases.
3. POST /cinema/payment - Make the payment for the particular booking
