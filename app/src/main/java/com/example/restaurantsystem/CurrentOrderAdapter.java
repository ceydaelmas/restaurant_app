package com.example.restaurantsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CurrentOrderAdapter  extends ArrayAdapter<String> {
    Context context;
    List<String> date;
    List<String> time;
    List<String> priceList;
    List<String> cardList;
    List<String> addressList;


    ImageView images;
    TextView myDate;
    TextView myTime;
    TextView myTotalPrice;
    TextView details;

    String orderID;

    Button cancelButton;
    String createdDate,createdTime;

    private FirebaseAuth mAuth;
    DatabaseReference currentOrderListRef;

    int icon;

    public CurrentOrderAdapter(Context context, List<String> date, List<String> time,int icon, List<String> priceList,List<String> cardList,List<String> addressList) {
        super(context,R.layout.card_item_currentorder,R.id.currentorders_textView_date_cardItem,date);
        this.context = context;
        this.date = date;
        this.time = time;
        this.icon = icon;
        this.priceList=priceList;
        this.cardList=cardList;
        this.addressList=addressList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View custom = layoutInflater.inflate(R.layout.card_item_currentorder,parent,false);

        mAuth = FirebaseAuth.getInstance();
        String a=mAuth.getCurrentUser().getUid();

        currentOrderListRef= FirebaseDatabase.getInstance().getReference().child("Users").child(a)
                .child("Order List").child("Current Orders");

        images = custom.findViewById(R.id.currentorders_image_cardItem);
        myDate = custom.findViewById(R.id.currentorders_textView_date_cardItem);
        myTime = custom.findViewById(R.id.currentorders_textView_time_cardItem);
        details = custom.findViewById(R.id.textView_products_currentorders);
        myTotalPrice = custom.findViewById(R.id.textView_totalPrice_currentOrder);

        cancelButton = custom.findViewById(R.id.cancel_carditem_button_currentOrders);

        String totaltext = String.valueOf(priceList.get(position));
        images.setImageResource(icon);
        myDate.setText(date.get(position));
        myTime.setText(time.get(position));
        myTotalPrice.setText("$"+totaltext);

        createdDate = myDate.getText().toString();
        createdTime = myTime.getText().toString();

        String saveCurrentDate;
        Calendar calForDate = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        if(!createdDate.equals(saveCurrentDate)){
            movePastOrder(position,totaltext);
        }

        //cancel current order
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(position);
            }
        });

        // show products of order
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductPop.class);
                orderID = "created on "+date.get(position)+" "+time.get(position);
                intent.putExtra("orderID", orderID);

                context.startActivity(intent);
            }
        });
        return custom;
    }

    //move current order to past order when order date is past
    private void movePastOrder(int position,String totaltext) {

        final HashMap<String, Object> orderMap = new HashMap<>();

        String a=mAuth.getCurrentUser().getUid();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getInstance().getReference().child("Users").child(a).child("Order List");
        orderMap.put("date",date.get(position));
        orderMap.put("time",time.get(position));
        orderMap.put("totalPrice",totaltext);
        orderMap.put("cardName",cardList.get(position));
        orderMap.put("addressName",addressList.get(position));

        String orderID = "created on "+date.get(position)+" "+time.get(position);
        cartListRef.child("Past Orders").child(orderID).updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

        //remove from current order when order date is past
        currentOrderListRef.child(orderID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(),"Order removed successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //remove current order from Current Order List
    private void remove(int position) {
        String orderID = "created on "+date.get(position)+" "+time.get(position);
        currentOrderListRef.child(orderID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(),"Order removed successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}