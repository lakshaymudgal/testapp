package com.example.viz_it;


    public class Upload {
        private String ImageUrl;
        private String PhoneNumber;

        public Upload() {
            //empty constructor needed
        }

        public Upload(String imageUrl){
                ImageUrl = imageUrl;
        }

        public Upload(String imageUrl, String phoneNumber) {

            ImageUrl = imageUrl;
            PhoneNumber = phoneNumber;
        }

        public String getPhoneNumber()
        {
            return PhoneNumber;
        }

        public void setPhoneNumber(String phoneNumber)
        {
            PhoneNumber = phoneNumber;
        }

        public String getImageUrl()
        {
            return ImageUrl;
        }

        public void setImageUrl(String imageUrl)
        {
            ImageUrl = imageUrl;
        }
    }
