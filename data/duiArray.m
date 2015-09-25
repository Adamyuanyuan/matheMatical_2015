function duiArray(M)
	length = size(M,1);
	% disp(length)
	
	for i = 1:length
		for j = 1:length
			if M(i,j) > 0
				M(j,i) = M(i,j)
			end
		end
	end
end 
