import firebase_admin
from firebase_admin import credentials, firestore
import requests
import random
import string
from dotenv import load_dotenv
import os


# Set Firebase service account key path
cred = os.getenv("cred")
firebase_admin.initialize_app(cred)

# Create Firestore instance
db = firestore.client()

# 유치원 ID가 되는 String
def generate_random_string(length=10):
    letters = string.ascii_letters
    return ''.join(random.choice(letters) for _ in range(length))

# 데이터 셋 생성
def generate_random_data(petcare_type, start_number=1):
    
    # petcare_name 1 부터 시작    
    if petcare_type.lower() == 'h':
        petcare_name = f"호텔 {start_number}"
    elif petcare_type.lower() == 'k':
        petcare_name = f"유치원 {start_number}"
    start_number += 1
    
    # 업체 ID
    petcare_id = f"{petcare_type}_{generate_random_string(8)}"
    
    # 서울 내 좌표 찍기위해 위도 경도 범위 지정
    petcare_lat = random.uniform(37.4, 37.7)
    petcare_lng = random.uniform(126.8, 127.2)

    # .env 파일에서 환경 변수 로드
    load_dotenv()

    # API 키 불러오기
    api_key = os.getenv("KAKAO_API_KEY")
    print(api_key)
    # Convert coordinates to address (using Kakao API)
    kakao_api_url = "https://dapi.kakao.com/v2/local/geo/coord2address.json"
    headers = {
    "Authorization": f"KakaoAK {api_key}",
    "os": "python"
}
    # 위도 경도 찍은거 넣고 
    params = {
        "x": petcare_lng,
        "y": petcare_lat,
        # 지도 위도 경도 표준
        "input_coord": "WGS84",
    }
    response = requests.get(url=kakao_api_url, params=params, headers=headers)
    result = response.json()
    # Check if the 'documents' key does not exist or is an empty list
    if "documents" in result and result["documents"]:
        address_data = result["documents"][0]["address"]
        petcare_si = address_data.get("region_1depth_name", "Unknown")
        petcare_gu = address_data.get("region_2depth_name", "Unknown")
        petcare_dong = address_data.get("region_3depth_name", "Unknown")
        petcare_addr = address_data.get("address_name", "Unknown")
    else:
        print("No documents found in the result or empty 'documents' list.")
        petcare_si = "Unknown"
        petcare_gu = "Unknown"
        petcare_dong = "Unknown"
        petcare_addr = "Unknown"

    petcare_addr_detail = None
    petcare_img = None
    petcare_opening = f"{random.randint(7, 10)}:00"
    petcare_closing = f"{random.randint(18, 22)}:00"

    petcare_url = None
    petcare_description = "그냥 설명입니다."

    petcare_response = None

    return {
        "petcare_id": petcare_id,
        "petcare_si": petcare_si,
        "petcare_gu": petcare_gu,
        "petcare_dong": petcare_dong,
        "petcare_addr": petcare_addr,
        "petcare_addr_detail": petcare_addr_detail,
        "petcare_img": petcare_img,
        "petcare_opening": petcare_opening,
        "petcare_closing": petcare_closing,
        "petcare_url": petcare_url,
        "petcare_description": petcare_description,
        "petcare_lat": petcare_lat,
        "petcare_lng": petcare_lng,
        "petcare_name": petcare_name,
        "petcare_response": petcare_response,
        "petcare_type": petcare_type,
    }

start_number = 1

# Add data to Firestore
for _ in range(200):
    # hotel data
    hotel_data = generate_random_data("h", start_number)
    doc_ref = db.collection("TB_PETCARE").document(hotel_data["petcare_id"])
    doc_ref.set(hotel_data)
    print(f"Hotel Data added: {hotel_data}")
    start_number += 1


start_number = 1

for _ in range(200):
        
    # kindergarten data
    kindergarten_data = generate_random_data("k", start_number)
    doc_ref = db.collection("TB_PETCARE").document(kindergarten_data["petcare_id"])
    doc_ref.set(kindergarten_data)
    print(f"Kindergarten Data added: {kindergarten_data}")
    start_number += 1


print("Script completed.")
